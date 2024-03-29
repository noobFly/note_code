package com.noob.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * 将输入流重新封装. 需要在spring拦截器中额外获取请求体信息时需要重新封装， 流式一次性的，
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
            ServletRequest requestWrapper = new CustomHttpServletRequestWrapper((HttpServletRequest) request);
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig config) {
        // do nothing
    }


}