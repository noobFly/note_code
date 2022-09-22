package com.noob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
@SpringBootApplication
@EnableEurekaServer
public class DatalakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatalakeApplication.class, args);
	}

}
