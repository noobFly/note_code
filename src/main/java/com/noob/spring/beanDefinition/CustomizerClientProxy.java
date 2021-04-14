package com.noob.spring.beanDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomizerClientProxy implements InvocationHandler {
	private Object instance;
	private Class<?> targetClass;
	private String targetClassName;
	private BeanFactory beanFactory;
	private static ObjectMapper om = new ObjectMapper();
	private static Logger logger = LoggerFactory.getLogger(CustomizerClientProxy.class);
	private Map<String, Method> methodCache = new ConcurrentHashMap<String, Method>();
	static {
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.setSerializationInclusion(Include.NON_NULL);
	}

	public CustomizerClientProxy(Class<?> targetClass, BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		CustomizerClient annotation = targetClass.getAnnotation(CustomizerClient.class);
		if (annotation == null) {
			throw new RuntimeException(
					targetClass.getName() + "未声明注解" + CustomizerClient.class.getName());
		}

		this.targetClass = annotation.targetClass();
		this.targetClassName = targetClass.getName();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		Method proxyMethod = methodCache.computeIfAbsent(methodName, name -> {
			try {
				return targetClass.getMethod(name, String.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		if (instance == null) {
			instance = getBean(this.targetClass);
		}
		Instant start = Instant.now();
		String paramStr = om.writeValueAsString(args[0]);
		logger.info("请求  clientName:{},  methodName:{}, 参数：{}", targetClassName, methodName, paramStr);
		String resultStr = (String) proxyMethod.invoke(instance, paramStr);
		logger.info("响应 cost:{}ms, 结果：{}", Duration.between(start, Instant.now()).toMillis(), resultStr);

		return resultStr;
	}

	private Object getBean(Class<?> webServiceInterface) {
		return beanFactory.getBean(webServiceInterface);
	}
}
