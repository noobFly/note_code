package com.noob.spring.beanDefinition;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

public class ReflectionUtilTest {
	public static void main(String[] args) {
		Method[] methods = ReflectionUtils.getAllDeclaredMethods(A.class);
		System.out.println(methods);
		
		Method a = AopUtils.getMostSpecificMethod(methods[3], A.class);
	}

	static class A extends B {

		private void testA() {
		};

		public void testB() {
		}

	}

	static abstract class B implements C {
		public abstract void testB();

		public void testC() {
		};

	}

	static interface C {
		default void testDefault() {
		};

		void testC();
	}
}
