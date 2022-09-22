
package com.noob.gateway;

import lombok.Data;

@Data
public class RequestLog {
    //columns START
    /**
     * id
     */
    private Long id;
    /**
     * requestName
     */
    private String requestName;
    /**
     * requestMethod
     */
    private String requestMethod;
    /**
     * requestParams
     */
    private String requestParams;
    /**
     * responseValue
     */
    private String responseValue;
    /**
     * ip
     */
    private String ip;
    /**
     * createTime
     */
    private java.util.Date createTime;
    private String userId;
}

