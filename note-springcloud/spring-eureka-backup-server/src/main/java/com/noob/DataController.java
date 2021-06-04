package com.noob;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("data")
public class DataController {

	@RequestMapping("collect")
	public String collect(@RequestParam("log") String log) {
		System.out.println("DataController " + log);
		return "collect " + log;
	}
}
