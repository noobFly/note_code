package com.noob.proxy.jdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class TestJDK {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		IEntity entity = new EntityJdk();
		EntityProxyHandler handler = new EntityProxyHandler(entity);
		Class clazz = Proxy.getProxyClass(entity.getClass().getClassLoader(), entity.getClass().getInterfaces());
		Constructor constructor = clazz.getConstructor(InvocationHandler.class);

		IEntity proxy1 = (IEntity) constructor.newInstance(new Object[] { handler }); // Proxy.class的构造方法需要一个InvocationHandler对象
		IEntity proxy2 = (IEntity) Proxy.newProxyInstance(IEntity.class.getClassLoader(), EntityJdk.class.getInterfaces(),
				handler);
		proxy1.test();
		proxy2.test();
		proxy1.testdefault();
		proxy2.testdefault();

	}
}
