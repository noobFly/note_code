package com.noob.servlet;

import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 需要在spring拦截器中获取请求体信息时必须加此拦截器
 *
 */
public class RequestBodyWrapperFilter implements Filter {

    @Override
    public void destroy() {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (request instanceof HttpServletRequest && ("POST".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) || "PUT".equalsIgnoreCase(((HttpServletRequest) request).getMethod()) && request.getContentType() != null && request.getContentType().contains("application/json")) {
            ServletRequest requestWrapper = new RestfulServletRequestWrapper((HttpServletRequest) request);
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // do nothing
    }

    /**
     * 重写获取流的方式
     *
     */
    private static class RestfulServletRequestWrapper extends HttpServletRequestWrapper {
        private byte[] body;       // 报文体
        private String charsetName; // 编码字符集

        public RestfulServletRequestWrapper(HttpServletRequest request) {
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
            return new BufferedReader(new InputStreamReader(getInputStream(), charsetName));
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
}