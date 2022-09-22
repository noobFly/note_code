package com.noob;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.noob.spring.beanDefinition.CustomizerClientImportBeanDefinitionRegistrar.BeanDefinitionRegistrarForImport;
@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
//@EnableTransactionManagement
//@Import(BeanDefinitionRegistrarForImport.class)
public class BootstrapApplication {
	public static void main(String[] args) {
	
		SpringApplication.run(BootstrapApplication.class, args);
	}
}
