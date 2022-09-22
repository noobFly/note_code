package com.noob.proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.AopUtils;

import com.noob.proxy.cglib.EntityCglib;
import com.noob.proxy.jdk.EntityJdk;
import com.noob.proxy.jdk.IEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 通过执行发现，A内部调用B 并不会触发拦截器执行。
 * <p>
 * 因为在CglibAopProxy.getProxy创建代理对象使用了CglibAopProxy.DynamicAdvisedInterceptor来包装Callback;
 * CglibAopProxy.ProxyCallbackFilter作为拦截器的过滤器
 * <p>
 * 在执行时，DynamicAdvisedInterceptor.intercept(这个方法体内会通过org.springframework.aop.TargetSource创建原始target对象)
 * ->
 * org.springframework.aop.framework.CglibAopProxy.CglibMethodInvocation.CglibMethodInvocation.proceed()
 * -> org.aopalliance.intercept.MethodInterceptor.invoke (业务自定义) ->
 * CglibAopProxy.CglibMethodInvocation.proceed ->
 * CglibAopProxy.CglibMethodInvocation.invokeJoinpoint
 * <p>
 * 这里最终变成了对真实target对象的org.springframework.cglib.proxy.MethodProxy.invoke
 * <p>
 * 所以 A方法内部执行B方法时 不再触发拦截器
 * 
 * @author admin {@link org.springframework.context.annotation.SpringCglibProxy}
 *
 */
@Slf4j
public class SpringAopProxyCustomizer {
	/**
	 * ProxyFactoryBean.getObject() 执行 DefaultAopProxyFactory.createAopProxy 创建代理对象
	 * 
	 * @param bean       真实target对象。
	 * @param cglibProxy 是否开启cglib代理。但真正是否使用需要后续判定
	 * @return
	 */
	public static ProxyFactoryBean proxyFactoryBean(Object bean, boolean cglibProxy, Class<?> targetClass) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		// 代理类型， 这里设定为true并不表示一定走cglib. 还有其他判定条件:
		// 见ProxyFactoryBean.getObject -> ProxyFactoryBean.getSingletonInstance -> ProxyFactoryBean.getProxy -> DefaultAopProxyFactory.createAopProxy
		factory.setProxyTargetClass(cglibProxy); 
		factory.addAdvice(new MethodInterceptor() {
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				log.info("MethodInterceptor invoke: {}", invocation.getMethod().getName());
				return invocation.proceed();
			}
		});
	 factory.setTarget(bean); // 源对象实例
	 factory.setTargetClass(targetClass); // 也可以设置对象类型 ，根据源码分析，都是给ProxyFactoryBean内的属性TargetSource设置。 应该是只有最后的设置属性才生效的
		return factory;
	}

	public static void main(String[] args) {
		testInterface(true);

		testClass(true);
	}

	/**
	 * AopUtils 的代理判定前提是： 一定要满足对象是 org.springframework.aop.SpringProxy
	 * <p>
	 * 代理对象是： com.noob.proxy.jdk.EntityJdk$$EnhancerBySpringCGLIB$$1b6a1486
	 */
	private static void testInterface(boolean isProxyTargetClass) {
	 IEntity proxy = (IEntity) SpringAopProxyCustomizer.proxyFactoryBean(new EntityJdk(), isProxyTargetClass, null).getObject();
	//		IEntity proxy = (IEntity) SpringAopProxyCustomizer.proxyFactoryBean(null, isProxyTargetClass, EntityJdk.class).getObject();

		log.info(
				"Interface: Target:{}, Proxy:{},  isProxyTargetClass: {}, isAopProxy:{}, isJdkDynamicProxy:{}, isCglibProxy:{}",
				AopUtils.getTargetClass(proxy).getName(), proxy.getClass().getName(), isProxyTargetClass,
				AopUtils.isAopProxy(proxy), AopUtils.isJdkDynamicProxy(proxy), AopUtils.isCglibProxy(proxy));
		proxy.test();
	}

	private static void testClass(boolean isProxyTargetClass) {
		EntityCglib proxy = (EntityCglib) SpringAopProxyCustomizer.proxyFactoryBean(new EntityCglib(), isProxyTargetClass, null)
				.getObject();
		log.info(
				"Class: Target:{}, Proxy:{},  isProxyTargetClass: {}, isAopProxy:{}, isJdkDynamicProxy:{}, isCglibProxy:{}",
				AopUtils.getTargetClass(proxy).getName(), proxy.getClass().getName(), isProxyTargetClass,
				AopUtils.isAopProxy(proxy), AopUtils.isJdkDynamicProxy(proxy), AopUtils.isCglibProxy(proxy));
		proxy.testPublic();
	}
}
