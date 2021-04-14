package com.noob.spring.beanDefinition;

import java.lang.reflect.Proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import com.noob.request.component.ITestTransactionOnInterfaceService;

import lombok.Getter;
import lombok.Setter;

public class CustomizerClientFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {
	@Getter
	@Setter
	private Class<T> clas;
	@Getter
	@Setter
	private boolean proxyTargetClass = false;
	private BeanFactory beanFactory;

	public CustomizerClientFactoryBean(Class<T> clas) {
		this.clas = clas;
		proxyTargetClass = clas.getAnnotation(CustomizerClient.class).proxyTargetClass(); // 最终会被BeanDefinition里的属性集合propertyValues覆盖
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {
		return proxyTargetClass ? createProxy(clas, beanFactory)
				: (T) Proxy.newProxyInstance(clas.getClassLoader(), new Class<?>[] { clas },
						new CustomizerClientProxy(clas, beanFactory));
	}

	public  static <T>  T  createProxy(Class<T> clas, BeanFactory beanFactory) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		factory.setProxyTargetClass(false); //  这里设置使用Cglib代理, 但还需要符合其他条件. 【（指定proxyTargetClass == true ||  没有指定代理接口）&&  targetClass 非接口类型 &&  targetClass 非JDK代理类 】时，执行Cglib代理；其他情况下都是用JDK代理。
		factory.addAdvice(new MethodInterceptor() {
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				return new CustomizerClientProxy(clas, beanFactory);
			}
		});
		factory.setTargetClass(clas);
		return (T) factory.getObject(); // 执行DefaultAopProxyFactory.createAopProxy 会选择走JdkDynamicAopProxy或ObjenesisCglibAopProxy
	}

	@Override
	public Class<?> getObjectType() {
		return clas;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public static void main(String[] args) {
		ITestTransactionOnInterfaceService a = CustomizerClientFactoryBean
				.createProxy(ITestTransactionOnInterfaceService.class, null);
		System.out.println(a);
	}
}
