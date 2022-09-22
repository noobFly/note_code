package com.noob;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("data")
public class DataController {

	@RequestMapping("collect")
	public String collect(@RequestParam("log") String msg) {
		log.info("DataController " + msg);
		return "collect " + msg;
	}
}
