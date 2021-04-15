package com.noob.request.component;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

 @Component
public class TestBeanPostProcessor implements BeanPostProcessor {
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("CService")) {
			// return new String(); //只有完全没有被其他类引用的bean才能换类型。否则在引用校验类型时会报错
			/**
			 * Caused by: org.springframework.beans.factory.BeanNotOfRequiredTypeException: Bean named 'CService' is expected to be of type 'com.noob.request.component.CService' but was actually of type 'java.lang.String'
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1258) ~[spring-beans-5.1.7.RELEASE.jar:5.1.7.RELEASE]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1168) ~[spring-beans-5.1.7.RELEASE.jar:5.1.7.RELEASE]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:593) ~[spring-beans-5.1.7.RELEASE.jar:5.1.7.RELEASE]
	... 19 common frames omitted
			 */
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (beanName.equals("CService")) {
			// return new String();
		}
		return bean;
	}
}
