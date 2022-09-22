package com.noob.gateway;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * 输出指定URL模式请求的日志
 * <p>
 * 把过滤器的bean注入到FilterRegistrationBean中，并设置一些属性，过滤的url，执行的顺序（order的数值越小，优先级越高）。
 * 这样就不用在该过滤器上添加注解@WebFilter(urlPatterns={})或@Configuration @Component等
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)

// @WebFilter(urlPatterns = {"/*"})
public class GatewayLogFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest proxyRequest = (HttpServletRequest) request;
            CurrentHttpServletResponseWrapper proxyResponse = new CurrentHttpServletResponseWrapper((HttpServletResponse) response);

            if (StringUtils.equalsAnyIgnoreCase(proxyRequest.getMethod(), "POST", "PUT")) {
                proxyRequest = new CurrentHttpServletRequestWrapper(proxyRequest);
            }

            long beginTime = System.currentTimeMillis();
            try {
                filterChain.doFilter(proxyRequest, proxyResponse);
            } finally {
                int consumeTime = (int) (System.currentTimeMillis() - beginTime);

                String requestContent = getRequestContent(proxyRequest);
                String responseContent = getResponseContent(proxyResponse);

                String requestURI = proxyRequest.getRequestURI();
                String queryString = proxyRequest.getQueryString();
                if (StringUtils.isNotBlank(queryString)) {
                    requestURI += "?" + queryString;
                }
                //TODO 异步保存网关请求日志 最好是执行前先写一次， 执行后再更新一次

            }
            return;
        }

        //默认
        filterChain.doFilter(request, response);
    }


    private String getRequestContent(HttpServletRequest request) throws IOException {
        String method = request.getMethod();
        if (!StringUtils.equalsAnyIgnoreCase(method, "POST", "PUT")) { //没有请求体数据
            return null;
        }

        String contentType = request.getHeader("Content-Type");

        if (StringUtils.isNotBlank(contentType)) {
            if (contentType.contains("multipart/form-data")) { //上传文件，不处理
                return null;
            }
        }

        CurrentHttpServletRequestWrapper requestWrapper = (CurrentHttpServletRequestWrapper) request;

        return IOUtils.readLines(requestWrapper.getReader()).stream().collect(Collectors.joining(" "));
    }


    private String getResponseContent(CurrentHttpServletResponseWrapper proxyResponse) {
        String contentType = proxyResponse.getContentType();
        // 文件流等形式的响应数据不保存
        if (StringUtils.isNotBlank(contentType) && StringUtils.containsAny(contentType, "application/json", "application/xml")) {
            byte[] byteData = proxyResponse.getContent();
            String charset =getCharsetFromContentType(contentType);

            return new String(byteData, Charset.forName(charset));
        }

        return null;
    }
    private String getCharsetFromContentType(String contentType) {
        String charset = null;
        if(StringUtils.isNotBlank(contentType) && contentType.indexOf(";") >= 0) {
            String[] splits = contentType.split(";");
            String charsetSpan = splits[1];
            if(charsetSpan != null && charsetSpan.startsWith("charset=")) {
                charset = charsetSpan.substring("charset=".length());
            }
        }
        if(StringUtils.isBlank(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }

    private static class CurrentHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private byte[] body;       // 报文体
        private String charsetName; // 编码字符集

        public CurrentHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            charsetName = request.getCharacterEncoding();
            try {
                body = IOUtils.toByteArray(request.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StringUtils.defaultString(charsetName, "UTF-8")));
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

    private static class ByteArrayServletOutputStream extends ServletOutputStream {

        protected final ByteArrayOutputStream buf;

        public ByteArrayServletOutputStream() {
            buf = new ByteArrayOutputStream();
        }

        public byte[] toByteArray() {
            return buf.toByteArray();
        }

        @Override
        public void write(int b) {
            buf.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            //
        }

        public void reset() {
            buf.reset();
        }
    }

    private static class CurrentHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private HttpServletResponse servletResponse;
        private byte[] byteData = new byte[0];

        private ByteArrayServletOutputStream outputStream = new ByteArrayServletOutputStream() {

            @Override
            public void flush() throws IOException {
                byte[] resultData = outputStream.toByteArray();
                if (resultData.length == 0) {
                    return;
                }

                servletResponse.getOutputStream().write(resultData);
                servletResponse.getOutputStream().flush();
                this.reset();

                if (byteData.length == 0) {
                    byteData = resultData;
                } else {
                    byteData = byteMerger(byteData, resultData);
                }
            }
        };

        public CurrentHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
            this.servletResponse = response;
        }

        public ServletOutputStream getOutputStream() {
            return outputStream;
        }

        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(outputStream);
        }

        public byte[] getContent() {
            return byteData;
        }
    }

    private static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


}
