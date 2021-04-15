package com.noob.request.component;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * 不支持 prototype 模式下的 field属性注入循环依赖
 *
 */
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class BService {
    @Autowired
	public CService cService;

	public String testAdvice() {
		System.out.println("目标方法testAdvice");
		if(new Random().nextBoolean()) {
			throw new RuntimeException("fail");
		}
		 return "testAdvice";
	}

	/*
	 * 不支持 构造器注入循环依赖
	 */
	// public BService(CService a) { }
}
