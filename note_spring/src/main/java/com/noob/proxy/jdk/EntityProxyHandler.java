package com.noob.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityProxyHandler implements InvocationHandler {
	private IEntity entity;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println(String.format("proxy invoke. method: %s, isProxy: %s", method.getName(),
				Proxy.isProxyClass(proxy.getClass())));
		return method.invoke(entity, args);
	}
}
