
package com.noob.controller.gateway;

import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

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

