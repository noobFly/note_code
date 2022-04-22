package com.noob.controller.gateway;

import lombok.Data;

@Data
public class PermissionDTO {
    /**
     * 编号，保留
     */
    private String code;
    /**
     * 接口地址
     */
    private String uri;
    /**
     * 接口名称
     */
    private String name;
    /**
     * 子系统ID，见sub_system表
     */
    private Long subSystemId;

}

