package com.noob;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, PersistenceExceptionTranslationAutoConfiguration.class})
@EnableConfigurationProperties
public class BootstrapApplication {
	public static void main(String[] args) {
	
		SpringApplication.run(BootstrapApplication.class, args);
	}
}
