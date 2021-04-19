package com.noob.request.component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * 验证执行顺序
 * <p>
 * 
 * 
 * @author admin
 *
 *
 */
//@Component
public class ExecuteSortComponent implements BeanFactoryAware, ApplicationContextAware, InitializingBean {
	@Getter
	private BService service;
	@Getter
	private String value;

	public ExecuteSortComponent(BService b) {
		System.out.println("这里是Constructor");
	}
	
	@Resource
	public void setServiceResource(BService service) {
		this.service = service;
		System.out.println("这里是@Resource");
	}

	@Value("${test.value}")
	public void setBeforeAutowired(String value) {
		this.value = value;
		System.out.println("setBeforeAutowired这里是@Value");
	}

	@Autowired
	public void setServiceAutowired(BService service) {
		this.service = service;
		System.out.println("这里是@Autowired");

	}

	@Value("${test.value}")
	public void setAfterAutowired(String value) {
		this.value = value;
		System.out.println("setAfterAutowired这里是@Value");
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println("这里是BeanFactoryAware");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		System.out.println("这里是ApplicationContextAware");
	}

	@PostConstruct
	public void init() {
		System.out.println("这里是@PostConstruct");
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("这里是InitializingBean接口");

	}


	public void initMethod() {
		System.out.println("这里是initMethod");
	}

}
