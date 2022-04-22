package com.noob.controller.gateway.openApi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "open-api-interceptor")
public class OpenApiInterceptorProperties {

    /**
     * 是否接收开发者中心主动通知 , 这里是主动推送方式， 也可以用应用定时自动拉取时效信息
     * TODO
     * 问题：
     * 1、应用要提供接收事件通知Handler的URI自动注入到开发者中心，
     * 2、开发者中心里需要维护与应用通讯的客户端，至少要能存储应用能被通知的方式。 如果太多应用接入，对开发者中心来说是个瓶颈。
     */
    private boolean acceptNotice = true;
    /**
     * 当前主机地址，如果不配置，则自动把本地网卡IP发给开发者中心
     */
    private String currentServerHost;
    /**
     * 是否开启应用绑定的渠道验证
     */
    private boolean verifyBindChannel = false;
    /**
     * 子系统编号
     */
    private String subSystem;
    /**
     * 是否开启权限验证
     */
    private boolean verifyPermission = false;
    /**
     * 拦截URL正则
     */
    private String urlPatterns;

    /**
     * 排除拦截URL正则
     */
    private List<String> excludeUrlPatterns;


    /**
     * 渠道值在请求参数中的默认key名
     */
    public static String CHANNEL_KEY = "channel";

    @PostConstruct
    public void init(){
        System.out.print(excludeUrlPatterns);
    }

}
