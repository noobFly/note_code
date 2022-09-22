package com.noob.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("server")
@Validated
public class DemoController {
    @RequestMapping("log")
    public String log(String msg, HttpServletRequest request) {
        log.info("DemoController log : {}  访问方式:{}", msg, request.getRequestURL());
        return "1";
    }

}
