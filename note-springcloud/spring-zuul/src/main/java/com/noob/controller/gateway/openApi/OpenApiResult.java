package com.noob.controller.gateway.openApi;

import lombok.Data;

@Data
public class OpenApiResult {

    private String rspCode;

    private String rspMsg;

    private String data;

    private long timestamp;

    private String sign;

    public boolean isSuccess() {
        return this.rspCode.equals("0");
    }
}
