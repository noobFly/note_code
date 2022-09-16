package com.noob.login;

import cn.hutool.core.exceptions.ValidateException;
import com.noob.json.JSON;
import com.noob.json.JSONObject;
import com.noob.util.security.MD5withRSAUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


/**
 * 设置请求体加密报文的解析过滤器
 *
 * @param systemProperties
 * @param cacheManager
 * @return
 * @Bean
 * public FilterRegistrationBean<EncryptedBodyFilter> encryptedBodyFilterBean(SystemProperties systemProperties, CacheManager cacheManager){
 * EncryptedBodyFilter encryptedBodyFilter = new EncryptedBodyFilter(SystemProperties.getPrivateKey(), (token) -> {
 * Cache cache = cacheManager.getCache(CacheKeys.CHALLEGE_TOKEN); // 每次申请一个令牌
 * Cache.ValueWrapper valueWrapper = cache.get(token);
 * if(valueWrapper.get() != null) {
 * cache.evict(token); // 只能使用一次
 * }
 * return valueWrapper.get() != null;
 * });
 * FilterRegistrationBean<EncryptedBodyFilter> bean = new FilterRegistrationBean<>(encryptedBodyFilter);
 * bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
 * bean.setUrlPatterns(Arrays.asList("/*"));
 * return bean;
 * }
 */

/**
 * 用在页面登录和注册场景,每次在调用业务接口前需要提前申请一个token用来校验操作合法。<br>
 * 将前端请求体内容加密内容解密<br>
 * 前端将整个json加密后通过请求体传输<br>
 * 并在请求头加上BODY_ENCRYPTED：true标识
 */
@Slf4j
public class EncryptedBodyFilter implements Filter {
    private final String privateKey;
    private final TokenValidator tokenValidator;

    public EncryptedBodyFilter(String serverPrivateKey, TokenValidator tokenValidator) {
        this.privateKey = serverPrivateKey;
        this.tokenValidator = tokenValidator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && isBodyEncrypted((HttpServletRequest) request)) {
            HttpServletRequest req = (HttpServletRequest) request;

            String content = IOUtils.readLines(req.getInputStream(), getCharacterEncoding(req)).stream().collect(Collectors.joining());
            if (StringUtils.isNotBlank(content)) {
                try {
                    String decryptData = new String(MD5withRSAUtils.decryptByPrivateKey(content, privateKey), StandardCharsets.UTF_8);

                    JSONObject json = JSON.parseObject(decryptData);

                    String data = json.getString("data");
                    String token = json.getString("token");

                    if (!tokenValidator.validate(token)) {
                        throw new ValidateException("请求报文无效");
                    }

                    chain.doFilter(new CurrentHttpServletRequestWrapper(req, data), response);
                    return;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("解密请求报文异常", e);
                    throw new RuntimeException(e);
                }
            } else {
                chain.doFilter(new CurrentHttpServletRequestWrapper(req, null), response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    // request body 带有 Body-Encrypted 才需要加解密处理 !!!
    private boolean isBodyEncrypted(HttpServletRequest req) {
        return StringUtils.equalsAnyIgnoreCase(req.getMethod(), "POST", "PUT") && "true".equals(req.getHeader("Body-Encrypted"));
    }

    private Charset getCharacterEncoding(HttpServletRequest request) {
        String characterEncoding = request.getCharacterEncoding();
        if (StringUtils.isNotBlank(characterEncoding)) {
            return Charset.forName(characterEncoding);
        } else {
            return StandardCharsets.UTF_8;
        }
    }

    private class CurrentHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private byte[] body;       // 报文体
        private Charset charset = StandardCharsets.UTF_8; // 编码字符集

        public CurrentHttpServletRequestWrapper(HttpServletRequest request, String data) {
            super(request);
            this.body = data == null ? new byte[0] : data.getBytes(charset);
        }


        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {

                @Override
                public int read() throws IOException {
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

    @FunctionalInterface
    public interface TokenValidator {
        /**
         * 挑战令牌是否有效
         *
         * @return 是否有效
         */
        boolean validate(String token);
    }
}
