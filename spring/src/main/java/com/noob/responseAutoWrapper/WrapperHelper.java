package com.noob.responseAutoWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

// 在处理逻辑里指定响应描述和响应编码
public class WrapperHelper {
	private static final String RETURN_CODE_ATTRIBUTE = "RETURN_CODE";
	private static final String RETURN_MSG_ATTRIBUTE = "RETURN_MSG";
	
	private WrapperHelper(){
		super();
	}
	
	public static void setCode(int code){
		RequestContextHolder.currentRequestAttributes().setAttribute(RETURN_CODE_ATTRIBUTE, Integer.valueOf(code), RequestAttributes.SCOPE_REQUEST);
	}
	
	public static int getCode(){
	    Integer code = (Integer)RequestContextHolder.currentRequestAttributes().getAttribute(RETURN_CODE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		return code == null ? 200 : code;
	}
	
	public static void setMessage(String message){
		RequestContextHolder.currentRequestAttributes().setAttribute(RETURN_MSG_ATTRIBUTE, message, RequestAttributes.SCOPE_REQUEST);
	}
	
	public static String getMessage(){
	    String message = (String)RequestContextHolder.currentRequestAttributes().getAttribute(RETURN_MSG_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		return StringUtils.isEmpty(message) ? "操作成功" : message;
	}
	
	public static void set(int code, String message){
	    RequestContextHolder.currentRequestAttributes().setAttribute(RETURN_CODE_ATTRIBUTE, Integer.valueOf(code), RequestAttributes.SCOPE_REQUEST);
	    RequestContextHolder.currentRequestAttributes().setAttribute(RETURN_MSG_ATTRIBUTE, message, RequestAttributes.SCOPE_REQUEST);
	}
}
