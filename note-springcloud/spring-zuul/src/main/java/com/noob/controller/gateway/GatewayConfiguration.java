package com.noob.controller.gateway;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 指定的请求路径filter
@Configuration
public class GatewayConfiguration {

	@Bean
	public FilterRegistrationBean<GatewayLogFilter> gatewayLogFilterRegistrationBean(GatewayLogFilter gatewayLogFilter) {
        FilterRegistrationBean<GatewayLogFilter> registration = new FilterRegistrationBean<>(gatewayLogFilter,
                new ServletRegistrationBean[0]);
        registration.addUrlPatterns(new String[] { "/gateway/*" });	
        return registration;
    }
}
