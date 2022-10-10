package com.noob.gateway;

import com.google.common.collect.Lists;
import com.noob.gateway.openApi.DeveloperProperties;
import com.noob.gateway.openApi.OpenApiInterceptorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
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
        registration.setServletNames(Lists.newArrayList("gatewayLogFilter")); // 一个 Filter 拦截的资源可通过：Servlet 名称和资源访问的请求路径 来指定
        registration.addUrlPatterns(new String[]{"/gateway/*"});
        return registration;
    }
}
