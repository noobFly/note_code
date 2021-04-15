package com.noob.proxy.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * <p>
 * methodProxy.invokeSuper方法：object传入的是代理对象。 这种方式下： 方法A内部执行到B方法 可继续触发拦截器
 * <p>
 * methodProxy.invoke方法：
 * 需要扩展net.sf.cglib.proxy.MethodInterceptor实现类构造函数传入原始target对象。 这种方式下就
 * 方法A内部执行到B方法 不再会触发。 同时对protected方法报错： Exception in thread "main"
 * java.lang.IllegalArgumentException: Protected method: testProtected()V at
 * net.sf.cglib.proxy.MethodProxy.invoke(MethodProxy.java:209) at
 * com.noob.proxy.cglib.EntityInterceptor.intercept(EntityInterceptor.java:28)
 * at
 * com.noob.proxy.cglib.EntityCglib$$EnhancerByCGLIB$$8ed79bf3.testProtected(<generated>)
 * at com.noob.proxy.cglib.TestCglib.main(TestCglib.java:17)
 *
 */
public class EntityInterceptor implements MethodInterceptor {

	private Object target;

	public EntityInterceptor(Object target) {
		this.target = target;
	}

	@Override
	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		System.out.println(String.format("cglib intercept. methodName: %s, isProxy: %s", method.getName(),
				Enhancer.isEnhanced(object.getClass())));
		//  return methodProxy.invokeSuper(object, args); // 传入的必须是代理对象。 方法A内部执行到B 可触发拦截器
		  	 return methodProxy.invoke(target, args); // 必须是原始target对象。 方法 A内部执行到B 不再触发
	}

}

//方法拦截器
class EntityCallbackFilter implements CallbackFilter {
	/* 返回指定setCallbacks中callback数组的下标 */
	@Override
	public int accept(Method method) {
		if (method != null)
			return 0;
		else
			return 1;
	}

}