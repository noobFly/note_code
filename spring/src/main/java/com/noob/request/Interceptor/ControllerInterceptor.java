package com.noob.request.Interceptor;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Aspect
public class ControllerInterceptor {
	// 表达式
	@Pointcut(value = "@annotation(com.noob.request.Interceptor.OpLog)")
	// 签名
	public void opLogPointcut() {
	}

	//	@Around(value = "execution(@com.noob.request.Interceptor.OpLog * *.*(..)) && @annotation(opLog)", argNames = "opLog") // 另外的写法
	@Around(value = "opLogPointcut() && @annotation(opLog)")
	public Object aroundLog(ProceedingJoinPoint point, OpLog opLog) throws Throwable {
		Object obj = null;
		Object[] args = point.getArgs();
		printMethod(point);

		try {
			obj = point.proceed(args);
		} catch (Throwable e) {
			log.error("方法执行异常", e);
		}
		return obj;
	}

	private void printMethod(ProceedingJoinPoint point) {
		MethodSignature signature = (MethodSignature) point.getSignature();
		String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
		log.info(methodName);
	}

	@Around(value = "opLogPointcut() && (args(request, ..) || args(.., request))")
	public Object around2(ProceedingJoinPoint point, HttpServletRequest request) throws Throwable {
		Object obj = null;
		Object[] args = point.getArgs();
		printMethod(point);

		try {
			obj = point.proceed(args);
		} catch (Throwable e) {
			log.error("方法执行异常", e);
		}
		return obj;
	}

}
