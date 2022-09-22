package com.noob;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

// 测试地址： http://localhost:2000/server/filter?msg=13
@Slf4j
@RestController
@RequestMapping("server")
@Validated
public class ServerController {
	@Autowired
	DatalakeClient datalakeClient;

	@RequestMapping("log")
	public String log(String msg, HttpServletRequest request) {
		log.info("访问方式:{}", request.getRequestURL()); // http://192.168.99.1:2000/server/log
		log.info("ServerController log : {}", log); 
		return datalakeClient.collect(msg);
	}

	@RequestMapping("filter")
	public String filter(String msg) {
		return datalakeClient.filter(msg);
	}
}
