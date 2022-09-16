package com.noob.controller.gateway;

import com.noob.json.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 *  与 GatewayLogFilter 的优缺点：
 *  1、 Filter的执行顺序会先于Servlet, 所以这里可以提前做对request的处理， 但无法知晓具体是路由到哪个Controller哪个方法
 *  2、Interceptor虽然可以知晓具体是路由到哪个Controller哪个方法，但如果在DispatcherServlet里解析参数时校验失败, 则不会再向后执行具体方法，此时将无法进入Interceptor切面逻辑！
 */
@Aspect
@Component("ApiLogInterceptor")
public class ApiLogInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogInterceptor.class);


    @Pointcut("!execution(public * cn.InfoController.getFile(..)) "
            + "&& !execution(public * cn.BrokerController.*(..)) ")
    public void exeService() {
    }

    @AfterThrowing("exeService()")
    public void afterThrowing() {
        logger.error("api拦截出现异常了......");
    }

    @Around("exeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        String name = pjp.getTarget().getClass().getSimpleName();
        String method = pjp.getSignature().getName();
        logger.info(String.format("请求方法 :  %s -> %s", name, method));


        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String requestParams = "";
        Object result;
        try {
            Object[] params = pjp.getArgs();
            boolean isFile = request instanceof MultipartRequest;
            if (params.length > 0) { // 日志打印请求参数
                if (!isFile) {
                    for (Object paramObject : params) {
                        if (paramObject instanceof MultipartFile || paramObject instanceof MultipartRequest || paramObject instanceof HttpServletRequest) {
                            isFile = true;
                            break;
                        }
                    }
                }
                if (!isFile) {
                    requestParams = JSON.toJSONString(params);
                    logger.info("request_param : {}", requestParams);
                } else {
                    logger.info("request_param not execute Jackson!");
                }

            }
            result = pjp.proceed();
            String responseValue = JSON.toJSONString(result);
            logger.info("response_body: {}", responseValue);
            saveLog(name, method, request, requestParams, responseValue);
        } finally {
            logger.info("请求执行耗时：: {}ms", System.currentTimeMillis() - startTime);
        }
        return result;
    }

    private void saveLog(String name, String method, HttpServletRequest request, String requestParams,
                         String responseValue) {
        try {
            RequestLog requestLog = new RequestLog();
            requestLog.setCreateTime(new Date());
            requestLog.setIp(getIpAddress(request));
            requestLog.setRequestName(name);
            requestLog.setRequestMethod(method);
            requestLog.setRequestParams(requestParams);
            requestLog.setResponseValue(responseValue);
            Object userId = "1111"; // 访问用户ID
            if (userId != null) {
                requestLog.setUserId(String.valueOf(userId));
            }
            // TODO  requestLogService.add(requestLog);
        } catch (Exception e) {
            logger.warn("保存请求日志信息 异常", e);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
