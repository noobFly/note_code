package com.noob.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Spring 默认提供的是BasicErrorController来对/error进行处理
// 根据Accept头的内容，输出不同格式的错误响应。比如针对浏览器的请求生成html页面，针对其它请求生成json格式的返回。字段为accept的text/html的内容来判断
@RestController
public class ErrorHandlerController implements ErrorController{

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public Object error2() {
        return RandomUtils.nextBoolean() ? handleOpenApiException() : "error";
    }

    private Object handleOpenApiException() {
        String code = "999999";
        String message = "内部故障";
        String dataStr = "";
        String timestamp = String.valueOf(new Date().getTime());

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("rspCode", code);
        resultMap.put("rspMsg", message);
        resultMap.put("data", dataStr);
        resultMap.put("timestamp", timestamp);

        return resultMap;
    }

}
