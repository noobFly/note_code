package com.noob;

import java.util.List;

import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.noob.spring.beanDefinition.CustomizerClientImportBeanDefinitionRegistrar.BeanDefinitionRegistrarForImport;
@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties
@EnableTransactionManagement
//@Import(BeanDefinitionRegistrarForImport.class)
public class BootstrapApplication {
	public static void main(String[] args) {
		
		List<String>  list = Lists.newArrayList();
		list.add("a");
		for(String a : list) {
			System.out.println(a);
		}
		SpringApplication.run(BootstrapApplication.class, args);
	}
}
