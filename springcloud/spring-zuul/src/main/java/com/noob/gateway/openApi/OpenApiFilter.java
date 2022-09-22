package com.noob.gateway.openApi;

import cn.hutool.core.exceptions.ValidateException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.noob.servlet.CustomHttpServletResponseWrapper;
import com.noob.util.IpUtils;
import com.noob.util.security.MD5withRSAUtils;
import feign.Feign;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带开发者中心鉴权的访问拦截器。 因为外网走的是https, 所以对传入的报文并不是总体上加密！
 * <p>
 * 开发者中心做鉴权信息的配置化.
 * 入参： @link: RequestParamKey
 * 将 appId + data + timestamp拼接成一个字符串(utf-8编码)；对该字符串进行MD5withRSA签名算法运算，加密因子采用应用的私钥，得到即为本次通讯的签名值；
 * 在拦截器里解析出这些key属性值，并按相同规则拼接后以公钥验签！
 *  为什么还需要对数据进行加签名： 隔离并校验渠道的真实性、防篡改！
 *
 * </p>
 */
@Slf4j
public class OpenApiFilter implements Filter {

    @Autowired
    private DeveloperProperties developerProperties;
    @Autowired
    private OpenApiInterceptorProperties openApiInterceptorProperties;

    private ObjectMapper om = new ObjectMapper();

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final static String DEFAULT_CHARSET = "UTF-8";

    /**
     * 鉴权信息的缓存 TODO 这里可以扩展增加时效性 push + pull方式的优劣
     */
    private static Map<String, AppSecurityDTO> appSecurityMap = new ConcurrentHashMap<>();
    private static ThreadLocal<AppSecurityDTO> currentAppSecurity = new ThreadLocal<>();
    /**
     * 与开发者中心的通讯客户端
     */
    private ApplicationClient feginClient;

    public OpenApiFilter() {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void init(FilterConfig arg0) {
        //do nothing
    }

    @Override
    public void destroy() {
        // 这里可以用来关闭与开发者中心的通讯客户端 TODO
        appSecurityMap.clear();
        feginClient = null;

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            for (String pattern : openApiInterceptorProperties.getExcludeUrlPatterns()) { // 跳过拦截器的模糊路径匹配
                if (pathMatcher.match(pattern, ((HttpServletRequest) request).getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            try {
                String appId;
                String timestamp;
                String data;
                String sign;

                StringBuilder requestBody = new StringBuilder();

                String contentType = request.getContentType();
                if (StringUtils.isEmpty(contentType) || contentType.contains("application/json")) {
                    List<String> lines = IOUtils.readLines(request.getInputStream(), request.getCharacterEncoding() == null ? DEFAULT_CHARSET : request.getCharacterEncoding());
                    for (String line : lines) {
                        requestBody.append(line);
                    }

                    Map<String, String> param = om.readValue(requestBody.toString(), new TypeReference<Map<String, String>>() {
                    });

                    appId = param.get(RequestParamKey.APP_ID);
                    timestamp = param.get(RequestParamKey.TIMESTAMP);
                    data = param.get(RequestParamKey.DATA);
                    sign = param.get(RequestParamKey.SIGN);
                } else {
                    appId = request.getParameter(RequestParamKey.APP_ID);
                    timestamp = request.getParameter(RequestParamKey.TIMESTAMP);
                    data = request.getParameter(RequestParamKey.DATA);
                    sign = request.getParameter(RequestParamKey.SIGN);
                }
                assertNotBlank(appId, "appId参数不能为空");
                assertNotBlank(timestamp, "timestamp参数不能为空");
                assertNotBlank(sign, "sign参数不能为空");

                verifyApplication(appId);
                // 验证Ip白名单
                verifyIp((HttpServletRequest) request);
                // MD5withRSA 用公钥验证签名
                verifySign(appId, timestamp, data, sign);
                // 验证应用Id与绑定的渠道是否一致
                verifyBindChannel(data);
                verifyPermission(appId, ((HttpServletRequest) request).getRequestURI());

                // 包装一下扔回FilterChain
                filterChain.doFilter(new OpenApiHttpServletRequestWrapper((HttpServletRequest) request, appId, data), new OpenApiHttpServletResponseWrapper((HttpServletResponse) response));

            } catch (Exception e) {
                try {
                    handleException(e, (HttpServletResponse) response);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                return;
            } finally {
                currentAppSecurity.remove();
            }
            return;
        }


        filterChain.doFilter(request, response);
    }


    private void handleException(Exception e, HttpServletResponse response) throws Exception {
        log.error("", e);
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding(DEFAULT_CHARSET);

        int code = 500;
        String message = "系统故障";

        Throwable cause = e.getCause();
        if (e instanceof PlatformException) {
            code = ((PlatformException) e).getCode();
            message = e.getMessage();
        } else if (cause != null && cause instanceof PlatformException) {
            code = ((PlatformException) cause).getCode();
            message = cause.getMessage();
        }
        handleResult(response, code, message, null);
        response.getOutputStream().flush();

    }


    private void verifyApplication(String appId) {
        AppSecurityDTO appSecurity = getAppSecurity(appId);
        if (appSecurity.isApplicationForbidden()) {
            throw new PlatformException("应用已被禁止访问");
        }
        if (appSecurity.isDeveloperForbidden()) {
            throw new PlatformException("应用已被禁止访问【开发者被禁用】");
        }

        currentAppSecurity.set(appSecurity);
    }

    private void verifySign(String appId, String timestamp, String data, String sign) {
        AppSecurityDTO secretKey = getAppSecurity(appId);
        String origSignData = appId + Strings.nullToEmpty(data) + timestamp;

        try {
            if (!MD5withRSAUtils.verify(origSignData.getBytes(DEFAULT_CHARSET), secretKey.getAppPublicKey(), sign)) {
                throw new PlatformException("签名不正确");
            }
        } catch (Exception e) {
            throw new PlatformException("验签失败");
        }
    }

    private void verifyIp(HttpServletRequest request) {
        AppSecurityDTO appSecurity = currentAppSecurity.get();

        String ipAddr = IpUtils.getIpAddr(request);
        List<String> ipWhitelistList = appSecurity.getIpWhitelistList();
        if (ipWhitelistList != null) {
            for (String validIp : ipWhitelistList) {
                if (validIp.equals(ipAddr) || "*".equals(validIp)) {
                    return;
                }
            }
        }

        log.info("请求IP不在白名单内：" + ipAddr);

        throw new PlatformException("请求IP不在白名单内");
    }

    private void verifyBindChannel(String data) {
        if (!openApiInterceptorProperties.isVerifyBindChannel()) {
            return;
        }
        AppSecurityDTO appSecurity = currentAppSecurity.get();
        List<String> bindChannelList = appSecurity.getBindChannelList();
        if (CollectionUtils.isEmpty(bindChannelList)) {
            return;
        }
        if (StringUtils.isNotBlank(data) && data.startsWith("{")) {
            Map<String, Object> paramMap;
            try {
                paramMap = om.readValue(data, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                log.error("", e);
                return;
            }
            Object object = paramMap.get(OpenApiInterceptorProperties.CHANNEL_KEY);
            if (object != null && bindChannelList.stream().anyMatch(t -> object.equals(t))) {
                return;
            }
        }

        throw new ValidateException("请求渠道/来源参数未与应用（appId）绑定");
    }


    private void verifyPermission(String appId, String requestURI) {
        if (!openApiInterceptorProperties.isVerifyPermission()) {
            return;
        }
        //TODO 这里获取并校验其他安全要素
        throw new PlatformException(403, "没有接口访问权限");
    }

    private AppSecurityDTO getAppSecurity(String appId) {
        // ConcurrentHashMap.computeIfAbsent的内部实现是线程安全的。 通过定位槽点占位的synchronized(ReservationNode)（如果槽点里是个链表或树，这里sync的是队首元素, 相当于sync整个槽点） 来控制执行创建逻辑还是输出对象！
        return appSecurityMap.computeIfAbsent(appId, key -> {
            AppSecurityDTO appSecurity = null;
            ApplicationClient client = getApplicationClient();
            try {
                OpenApiResult apiResult = client.getApplicationSecurityInfo(developerProperties.getSystemId(), appId);
                if (!apiResult.isSuccess()) {
                    throw new BizException(Integer.valueOf(apiResult.getRspCode()), apiResult.getRspMsg());
                }

                appSecurity = om.readValue(apiResult.getData(), AppSecurityDTO.class);
            } catch (BizException e) {
                throw new BizException(500, "内部故障", String.format("向开发者中心获取应用%s的密钥信息失败 ！openError: %s %s", appId, e.getCode(), e.getMessage()), e);
            } catch (Exception e) {
                log.error("向开发者中心获取应用{}的密钥信息失败", appId, e);
            }
            return appSecurity;
        });
    }

    private ApplicationClient getApplicationClient() {
        if (feginClient == null) {
            feginClient = Feign.builder().decoder(new JacksonDecoder()).encoder(new FormEncoder(new JacksonEncoder())).target(ApplicationClient.class, developerProperties.getCenterHost());
        }
        return feginClient;
    }


    private String sign(int code, String message, String data, long timestamp) {
        String signData = code + Strings.nullToEmpty(message) + Strings.nullToEmpty(data) + timestamp;

        AppSecurityDTO appSecurity = currentAppSecurity.get();

        String sign;
        try {
            sign = MD5withRSAUtils.sign(signData.getBytes(DEFAULT_CHARSET), appSecurity.getPlatformPrivateKey());
        } catch (Exception e) {
            throw new PlatformException(e);
        }
        return sign;
    }


    private void assertNotBlank(String target, String errorMessage) {
        if (StringUtils.isBlank(target)) {
            throw new ValidateException(errorMessage);
        }
    }


    private class OpenApiHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private byte[] body;       // 报文体
        private String charsetName; // 编码字符集
        private String appId;
        Map<String, Object> paramMap = new HashMap<>();

        public OpenApiHttpServletRequestWrapper(HttpServletRequest request, String appId, String data) {
            super(request);
            this.appId = appId;
            charsetName = request.getCharacterEncoding();

            try {
                if (StringUtils.isEmpty(data)) {
                    body = new byte[0];
                } else {
                    body = data.getBytes(charsetName == null ? DEFAULT_CHARSET : charsetName);
                }

                if (StringUtils.isNotBlank(data) && data.startsWith("{")) {
                    paramMap = om.readValue(data, new TypeReference<Map<String, Object>>() {
                    });

                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Override
        public String getParameter(String name) {
            Object value = paramMap.get(name);
            return value == null ? null : String.valueOf(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            return new String[]{getParameter(name)};
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Enumeration<String> enu = getParameterNames();
            Map<String, String[]> result = new HashMap<>();
            while (enu.hasMoreElements()) {
                String name = enu.nextElement();
                result.put(name, getParameterValues(name));
            }
            return result;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            Vector<String> vector = new Vector<>();
            Iterator<String> iterator = paramMap.keySet().iterator();
            while (iterator.hasNext()) {
                vector.add(iterator.next());
            }
            return vector.elements();
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), charsetName == null ? DEFAULT_CHARSET : charsetName));
        }

        @Override
        public String getHeader(String name) {
            if ("Content-Type".equalsIgnoreCase(name) && body != null) {
                return "application/json";
            } else if ("App-Id".equalsIgnoreCase(name)) {
                return appId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("Content-Type".equalsIgnoreCase(name) && body != null) {
                Vector<String> vector = new Vector<>();
                vector.add("application/json");
                return vector.elements();
            } else if ("App-Id".equalsIgnoreCase(name)) {
                Vector<String> vector = new Vector<>();
                vector.add(appId);
                return vector.elements();
            }
            return super.getHeaders(name);
        }

        @Override
        public String getContentType() {
            if (body != null) {
                return "application/json";
            }
            return super.getContentType();
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body == null ? new byte[0] : body);
            return new ServletInputStream() {

                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // do nothing
                }
            };
        }
    }


    private class OpenApiHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private CustomHttpServletResponseWrapper.CustomOutputStream outputStream = new CustomHttpServletResponseWrapper.CustomOutputStream() {
            @Override
            public void flush() throws IOException {
                byte[] resultData = outputStream.toByteArray();
                if (resultData.length == 0) {
                    return;
                }

                getResponse().setContentType("application/json");

                String characterEncoding = getResponse().getCharacterEncoding();
                if (StringUtils.isBlank(characterEncoding)) {
                    getResponse().setCharacterEncoding(DEFAULT_CHARSET);
                }

                String resultStr = new String(resultData, DEFAULT_CHARSET);
                if (resultStr.startsWith("{")) {
                    Map<String, Object> result = om.readValue(resultStr, new TypeReference<Map<String, Object>>() {
                    });

                    handleResult(getResponse(), (Integer) result.get("code"), (String) result.get("message"), om.writeValueAsString(result.get("data")));
                } else {
                    getResponse().getOutputStream().write(resultData);
                }

                getResponse().getOutputStream().flush();
                this.reset();
            }
        };


        public OpenApiHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        public ServletOutputStream getOutputStream() {
            return outputStream;
        }

        public PrintWriter getWriter() {
            return new PrintWriter(outputStream);
        }

        @Override
        public String getCharacterEncoding() {
            return super.getCharacterEncoding();
        }
    }


    private void handleResult(ServletResponse servletResponse, Integer code, String message, String data) throws IOException {
        long timestamp = new Date().getTime();
        String sign = sign(code, message, data, timestamp);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rspCode", code);
        resultMap.put("rspMsg", message);
        resultMap.put("data", data);
        resultMap.put("timestamp", String.valueOf(timestamp));
        resultMap.put("sign", sign);
        String content = om.writeValueAsString(resultMap);
        servletResponse.getOutputStream().write(content.getBytes(servletResponse.getCharacterEncoding()));
    }
}
