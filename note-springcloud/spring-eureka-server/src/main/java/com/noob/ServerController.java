package com.noob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 测试地址： http://localhost:2000/server/filter?msg=13
@RestController
@RequestMapping("server")
public class ServerController {
	@Autowired
	DatalakeClient datalakeClient;

	@RequestMapping("log")
	public String log(String msg) {
		return datalakeClient.collect(msg);
	}

	@RequestMapping("filter")
	public String filter(String msg) {
		return datalakeClient.filter(msg);
	}
}
