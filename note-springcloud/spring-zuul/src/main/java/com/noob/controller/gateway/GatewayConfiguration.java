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
        FilterRegistrationBean<GatewayLogFilter> registration = new FilterRegistrationBean<>(gatewayLogFilter,
                new ServletRegistrationBean[0]);
        registration.addUrlPatterns(new String[]{"/gateway/*"});
        return registration;
    }
}
