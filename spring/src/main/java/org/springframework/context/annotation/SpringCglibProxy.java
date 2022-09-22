package org.springframework.context.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.noob.proxy.cglib.EntityCglib;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 对 @Bean 的处理 ：
 * org.springframework.context.annotation.ConfigurationClassPostProcessor
 * implement org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * <p>
 * ConfigurationClassPostProcessor.postProcessBeanFactory方法 ->
 * ConfigurationClassPostProcessor.enhanceConfigurationClasses
 * 中选择的是org.springframework.context.annotation.ConfigurationClassEnhancer来创建cglib代理（这个类很重要）
 * <p>
 * 定义的callback是org.springframework.context.annotation.ConfigurationClassEnhancer.BeanMethodInterceptor。
 * <p>
 * 在intercept方法中最终执行的"cglibMethodProxy.invokeSuper(enhancedConfigInstance,
 * beanMethodArgs)" 就是org.springframework.cglib.proxy.MethodProxy.invokeSuper
 * 
 * 所以 方法A执行到方法B 会再次触发拦截器
 * <p> 同时，该拦截方法逻辑的执行里，会优先判定容器中是否存在方法B已经创建（或可提前引用）的bean, 有则直接使用，否则创建新实例代理。
 * 
 * @author admin
 *
 */
@Slf4j
public class SpringCglibProxy {
	public static void main(String[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ConfigurationClassEnhancer configurationClassEnhancer = new ConfigurationClassEnhancer();
		Method method = Arrays.asList(configurationClassEnhancer.getClass().getDeclaredMethods()).stream()
				.filter(t -> "newEnhancer".equals(t.getName())).findFirst().get();
		method.setAccessible(true);
		Enhancer enhancer = (Enhancer) method.invoke(configurationClassEnhancer, EntityCglib.class,
				EntityCglib.class.getClassLoader());
		enhancer.setCallbacks(new Callback[] { new BeanMethodInterceptor(new EntityCglib()) });
		enhancer.setCallbackType(BeanMethodInterceptor.class);
		enhancer.setCallbackFilter(new CallbackFilter() {

			@Override
			public int accept(Method arg0) {
				return 0;
			}
		});
		EntityCglib proxy = (EntityCglib) enhancer.create();
		proxy.testPublic();

	}

	/**
	 * 此时方法入参obj是代理对象：com.noob.proxy.cglib.EntityCglib$$EnhancerBySpringCGLIB$$71a9bc50
	 *
	 */
	@AllArgsConstructor
	private static class BeanMethodInterceptor implements MethodInterceptor {
		private Object target;

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			log.info("MethodInterceptor invoke: {}, proxyClassisAopPorxy:{}， Proxy:{}, Target:{}", method.getName(),
					AopUtils.isAopProxy(obj), obj.getClass().getName(), AopUtils.getTargetClass(obj)); // 此时的AopUtils.getTargetClass是无法获取真实target的class的
			return proxy.invokeSuper(obj, args); // 可再次触发拦截器
			// return proxy.invoke(target, args); // 不可再次触发
		}

	}
}
