package com.noob.gateway;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

// 通过网络转发达到网关路由的效果
@Slf4j
@RestController
public class GatewayController {


    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;


    public GatewayController() throws Exception {
        hostnameVerifier = new ApiHostNameVerifier();

        SSLContext ssl = SSLContext.getInstance("SSL");
        ssl.init(null, new TrustManager[]{new ApiTrustManager()}, new SecureRandom());
        sslSocketFactory = ssl.getSocketFactory();
    }

    @RequestMapping("/gateway/**")
    public void route(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            //TODO 这里可以对请求访问的地址进行业务校验 req.getRequestURI()
            doProxy(request, response);
        } catch (Exception e) {
            log.error("代理路由出错：" + request.getRequestURI(), e);
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print("代理路由出错");
        }
    }


    private void doProxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = request.getRequestURI().replace("/gateway", "");
        ProxyPassInfo proxyPassInfo = getProxyPassInfoByUrl(requestUri);
        log.debug(requestUri);
        if (proxyPassInfo != null) {
            //填充自定义的HTTP头
            fillHttpHeader(request, proxyPassInfo);
            log.debug("{} proxy pass to {}", request.getRequestURI(), proxyPassInfo.getProxyPassUrl());
            if (log.isInfoEnabled()) {
                List<ProxyPassInfo.HttpHeader> httpHeaderList = proxyPassInfo.getHeaderList();
                for (ProxyPassInfo.HttpHeader httpHeader : httpHeaderList) {
                    log.debug("header: {} = {}", httpHeader.getName(), httpHeader.getValue());
                }
            }

            request.setAttribute("upstreamServer", proxyPassInfo.getUpstream()); //用于 GatewayLogFilter记录转发真实的服务器

            proxyPass(request, response, proxyPassInfo);
        } else {
            throw new RuntimeException("没有匹配的代理");
        }
    }

    //TODO 根据访问url来查找地址配置、HttpHeader请求头的配置。
    private ProxyPassInfo getProxyPassInfoByUrl(String requestUri) {
        return new ProxyPassInfo();
    }


    //TODO 这里需要优化使用池化HTTP
    private void proxyPass(HttpServletRequest req, HttpServletResponse response, ProxyPassInfo proxyPassInfo) throws Exception {
        String url = proxyPassInfo.getProxyPassUrl() + proxyPassInfo.getRequestUri();
        List<ProxyPassInfo.HttpHeader> headerList = proxyPassInfo.getHeaderList();
        Map<String, String> headParams = new HashMap<String, String>();
        for (ProxyPassInfo.HttpHeader httpHeader : headerList) {
            headParams.put(httpHeader.getName(), httpHeader.getValue());
        }

        String queryString = req.getQueryString();
        if (!Strings.isNullOrEmpty(queryString)) {
            url += "?" + queryString;
        }

        if ("normal".equalsIgnoreCase(proxyPassInfo.getProtocol())) {
            normalProxy(req, response, url, headParams);
        } else {
            //TODO 可以定义不同的数据交互协议, 对于类似文件之类流的传输需要特别处理！
            // proxyPassForUploadFile
            // proxyPassForDownloadFile
            throw new RuntimeException("未知的代理协议");
        }
    }

    private String proxyPassForUploadFile(HttpServletRequest req, HttpServletResponse resp, ProxyPassInfo proxyPassInfo, Map<String, String> headerParams) throws IOException {
        long startTime = System.currentTimeMillis();
        log.info("Send upload file request to grimgr ...");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
        Map<String, MultipartFile> files = multipartRequest.getFileMap();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = "";
        String urlPath = proxyPassInfo.getProxyPassUrl() + proxyPassInfo.getRequestUri();

        HttpPost httpPost = new HttpPost(urlPath);
        setHeaderParams(httpPost, headerParams);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            String filename = entry.getValue().getOriginalFilename();
            builder.addBinaryBody("file", entry.getValue().getBytes(), ContentType.MULTIPART_FORM_DATA, filename);
        }
        log.info("Prepare uploadFile {} MS", System.currentTimeMillis() - startTime);
        try {
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            // 执行提交
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            log.error("请求异常urlPath={}", urlPath);
        } finally {
            httpClient.close();
        }
        log.info("responseData:{}", result);
        return result;
    }

    private String proxyPassForDownloadFile(HttpServletRequest req, HttpServletResponse resp, ProxyPassInfo proxyPassInfo, Map<String, String> headerParams) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String urlPath = proxyPassInfo.getProxyPassUrl() + proxyPassInfo.getRequestUri();
        if (StringUtils.isNotEmpty(req.getQueryString())) {
            urlPath = urlPath + "?" + req.getQueryString();
        }

        log.info("Send download file request to grimgr, urlPath={}", urlPath);
        HttpGet httpGet = new HttpGet(urlPath);
        setHeaderParams(httpGet, headerParams);

        try {
            // 执行提交
            CloseableHttpResponse response = httpClient.execute(httpGet);

            //header转设，若返回不是文件，则不能添加Transfer-Encoding:chunked
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                log.info("header: {}={}", header.getName(), header.getValue());
                if ("Content-Disposition".equals(header.getName()) || "Content-Type".equals(header.getName())) {
                    resp.addHeader(header.getName(), header.getValue()); // 对象头需要透传接口提供者服务的返回
                }
            }

            //文件体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                log.info("download file returned, contentLength={}", responseEntity.getContentLength());
                responseEntity.writeTo(resp.getOutputStream());
            }
        } catch (Exception e) {
            log.error("请求异常urlPath={}", urlPath);
        } finally {
            httpClient.close();
        }
        return "";

    }

    private void setHeaderParams(HttpRequestBase httpBase, Map<String, String> headerParams) {
        Set<Map.Entry<String, String>> headerParamsSet = headerParams.entrySet();
        for (Map.Entry<String, String> entry : headerParamsSet) {
            httpBase.setHeader(entry.getKey(), entry.getValue());
        }
    }

    //直接代理，不转换请求内容
    private void normalProxy(HttpServletRequest req, HttpServletResponse gatewayResponse, String proxyUrl, Map<String, String> headParams) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        HttpURLConnection realHttpConnection;
        if (proxyUrl.startsWith("https")) {
            URL url = new URL(proxyUrl);

            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, new TrustManager[]{new ApiTrustManager()}, new SecureRandom());

            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            https.setSSLSocketFactory(sslSocketFactory);
            https.setHostnameVerifier(hostnameVerifier);
            realHttpConnection = https;

        } else {
            URL url = new URL(proxyUrl);
            realHttpConnection = (HttpURLConnection) url.openConnection();
        }

        if (StringUtils.equalsAnyIgnoreCase(req.getMethod(), "POST", "PUT")) {
            realHttpConnection.setDoOutput(true);
        }
        realHttpConnection.setDoInput(true);
        realHttpConnection.setRequestMethod(req.getMethod());

        //超时时间设置
        realHttpConnection.setConnectTimeout(2 * 1000); //毫秒
        realHttpConnection.setReadTimeout(30 * 1000);
        //部分原生请求头转发
        proxyRequstHeader(req, realHttpConnection, "Content-Type", "Accept");

        //设置自定义请求头
        if (headParams != null) {
            headParams.forEach((key, value) -> {
                realHttpConnection.setRequestProperty(key, value);
            });
        }
        //真实IP
        realHttpConnection.setRequestProperty("X-Forwarded-For", req.getRemoteAddr());

        //请求内容
        if (StringUtils.equalsAnyIgnoreCase(req.getMethod(), "POST", "PUT")) {
            IOUtils.copy(req.getInputStream(), realHttpConnection.getOutputStream());
            realHttpConnection.getOutputStream().flush(); // 推送请求至真实接口服务地址
        }

        // 将响应数据转发出去
        InputStream proxyInput = realHttpConnection.getInputStream();

        gatewayResponse.setStatus(realHttpConnection.getResponseCode());
        //响应头的原样输出
        proxyResponseHeader(gatewayResponse, realHttpConnection, "Content-Type", "Content-Encoding");

        //响应内容
        log.debug(String.format("响应码：%s", realHttpConnection.getResponseCode()));

        IOUtils.copy(proxyInput, gatewayResponse.getOutputStream());
        proxyInput.close();
        gatewayResponse.getOutputStream().flush(); // 返回数据给到外部调用方
    }


    private void proxyRequstHeader(HttpServletRequest req, HttpURLConnection conn, String... headers) {
        for (String header : headers) {
            String value = req.getHeader(header);
            if (StringUtils.isNotBlank(value)) {
                conn.setRequestProperty(header, value);
            }
        }
    }

    private void proxyResponseHeader(HttpServletResponse response, HttpURLConnection conn, String... headers) {
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        if (headerFields != null) {
            headerFields.forEach((key, value) -> {
                if (StringUtils.equalsAnyIgnoreCase(key, headers)) {
                    log.debug("KEY: {}, VALUE: {}", key, value);
                    response.setHeader(key, CollectionUtil.isEmpty(value) ? "" : value.get(0));
                }
            });
        }
    }

    private String getResponseCharset(String contentType) {
        String charset = "UTF-8";
        if (StringUtils.isNotBlank(contentType) && contentType.indexOf(";") >= 0) {
            String[] splits = contentType.split(";");
            String charsetSpan = splits[1];
            if (charsetSpan != null && charsetSpan.startsWith("charset=")) {
                charset = charsetSpan.substring("charset=".length());
            }
        }
        return charset;
    }

    //信任所有证书
    private static class ApiTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

    }

    //目前信任所有域名
    private class ApiHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }

    }


    //配置化的http头赋值
    private void fillHttpHeader(HttpServletRequest req, ProxyPassInfo proxyPassInfo) throws UnsupportedEncodingException {
        List<ProxyPassInfo.HttpHeader> headerList = proxyPassInfo.getHeaderList();
        List<ProxyPassInfo.HttpHeader> addHeaderList = new ArrayList<>();
        // 默认自带
        addHeaderList.add(new ProxyPassInfo.HttpHeader("userName", URLEncoder.encode("测试用户", "UTF-8")));

        if (!CollectionUtils.isEmpty(headerList)) {
            for (ProxyPassInfo.HttpHeader header : headerList) {
                if (header.getValue().startsWith("$")) { //目前仅支持传送当前用户的用户名，以及其它配置的静态请求头信息
                    if (!header.getValue().equalsIgnoreCase("$userName")) {
                        String tmp = header.getValue().replace("$", "");
                        header.setValue(req.getHeader(tmp));
                    }
                }
            }
            headerList.addAll(addHeaderList);
        }
    }

    /**
     * ProxyPass信息
     */
    @Data
    public static class ProxyPassInfo {

        //原请求地址
        private String requestUri;

        //转发地址
        private String proxyPassUrl;

        //是否需要session认证,1=是，0=否，-1=忽略
        private String needSessionAuth;

        //转发协议 可自定义宝文格式
        private String protocol;
        // 真实的接口服务器访问地址
        private String upstream;

        //http头列表，注意该头列表中value为变量名
        private List<HttpHeader> headerList = new ArrayList<HttpHeader>();

        public void addHttpHeader(HttpHeader httpHeader) {
            this.headerList.add(httpHeader);
        }

        //http头
        @Data
        @AllArgsConstructor
        public static class HttpHeader {
            private String name;
            private String value;
        }
    }
}
