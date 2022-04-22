package com.noob.controller.gateway.openApi;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

// 连接开发者中心的FeginClient
public interface ApplicationClient {
    @Headers("Content-Type: application/json")
    @RequestLine("POST /open/application/getApplicationSecurityInfo")
    public OpenApiResult getApplicationSecurityInfo(@Param("systemId") String systemId, @Param("appId") String appId);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /open/application/getAppPermissions")
    public OpenApiResult getAppPermissions(@Param("appId") String appId, @Param("system") String system);
}
