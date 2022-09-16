package com.noob.controller.gateway;

import com.noob.controller.gateway.openApi.DeveloperProperties;
import com.noob.controller.gateway.openApi.OpenApiInterceptorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 指定的请求路径的转发路由filter
@Configuration
@EnableConfigurationProperties({
        DeveloperProperties.class,
        OpenApiInterceptorProperties.class})
public class GatewayConfiguration {

    @Bean
    public FilterRegistrationBean<GatewayLogFilter> gatewayLogFilterRegistrationBean(GatewayLogFilter gatewayLogFilter) {
        FilterRegistrationBean<GatewayLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(gatewayLogFilter);
        registration.setServletNames(null); // 一个 Filter 拦截的资源可通过：Servlet 名称和资源访问的请求路径 来指定
        registration.addUrlPatterns(new String[]{"/gateway/*"});
        return registration;
    }
}
