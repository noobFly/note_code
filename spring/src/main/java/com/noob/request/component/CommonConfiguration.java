package com.noob.request.component;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.AnnotationMetadata;


public class CommonConfiguration implements ImportAware {
	@Bean
	public Properties properties(@Qualifier("getBservice2") BService bService) {
		return new Properties();
	}
	
	@RefreshScope
	@Bean
	@Primary
	public BService bservice() {
		BService bService = new BService();
		return bService; // 3fe512d2
	}

	@Bean
	public BService getBservice2() {
		BService bService = new BService();
		return bService;
	}

	@Bean
	public Properties properties2() {
		BService bService = bservice();
		return new Properties();
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		System.out.println();

	}

}
