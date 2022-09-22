package com.noob.controller;

import org.apache.catalina.Globals;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring 默认提供的是BasicErrorController来对/error进行处理
 * 根据Accept头的内容，输出不同格式的错误响应。比如针对浏览器的请求生成html页面，针对其它请求生成json格式的返回。字段为accept的text/html的内容来判断
 * <p>
 * 当请求被DispatcherServlet处理完成后，回到StandardHostValve#invoke 后半段 -> StandardHostValve#status: </p>
 * <p> response.isError() 为 true 依据的statusCode 去找ErrorPage, 找不到则取StandardContext.ErrorPageSupport里的默认配置的‘/error’ ,
 * 重新设置好request (新增ERROR和转发相关属性)和response (  response.resetBuffer(true); response.setContentLength(-1); 这里对于response会"keeping the real error code and message" ) 后
 * StandardHostValve#custom -> ApplicationDispatcher#forward 再次转发进入DispatcherServlet 找‘/error’
 * </p>
 */

@RestController
public class ErrorHandlerController implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public Object error(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer sb = new StringBuffer().append(RequestDispatcher.ERROR_STATUS_CODE + ": " + request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).append("\n")
                .append(RequestDispatcher.ERROR_MESSAGE + ": " + request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).append("\n")
                .append(Globals.DISPATCHER_REQUEST_PATH_ATTR + ": " + request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR)).append("\n")
                .append(Globals.DISPATCHER_TYPE_ATTR + ": " + request.getAttribute(Globals.DISPATCHER_TYPE_ATTR)).append("\n")
                .append(RequestDispatcher.ERROR_SERVLET_NAME + ": " + request.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME)).append("\n")
                .append(RequestDispatcher.ERROR_REQUEST_URI + ": " + request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).append("\n")
                .append("response statusCode: " + response.getStatus());

        return sb.toString(); // 经过验证，request、和response 最底层的（org.apache.catalina.connector 层的 RequestFacade、 ResponseFacade）是会循环使用的！
    }
}
