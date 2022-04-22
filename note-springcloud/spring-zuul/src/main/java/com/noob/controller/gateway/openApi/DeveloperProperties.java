package com.noob.controller.gateway.openApi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@ConfigurationProperties(prefix = "developer")
public class DeveloperProperties {
    // 分配当前服务器的ID
    private String systemId;
    // 开发者中心地址   TODO 开发者中心单点或性能瓶颈是个问题！最好是个域名
    private String centerHost;
}
