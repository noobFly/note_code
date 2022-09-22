package com.noob.request.Interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdviceInterceptor {
	@Pointcut(value = "execution(public String com.noob.request.component.BService.testAdvice(..))")
	public void pointcut() {}

	@Before("pointcut()")
	public void before(JoinPoint point) {
		System.out.println("before");
	}

	@Around("pointcut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		System.out.println("around begin");
		try {
			Object proceed = point.proceed();
			System.out.println("around after");
			return proceed;
		} catch (Throwable e) {
			System.out.println("around after exception");
			throw e;
		}
	}

	@After("pointcut()")
	public void after() throws Throwable {
		System.out.println("after");
	}

	@AfterReturning("pointcut()")
	public void afterReturning() throws Throwable {
		System.out.println("afterReturning");
	}

	@AfterThrowing("pointcut()")
	public void afterThrowing() {
		System.out.println("afterThrowing");
	}
}
