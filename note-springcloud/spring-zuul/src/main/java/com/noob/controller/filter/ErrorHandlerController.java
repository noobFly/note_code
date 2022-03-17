package com.noob.controller.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.zuul.context.RequestContext;

//TODO 暂时还没测试通过ErrorController
@RestController
public class ErrorHandlerController implements ErrorController {

	@Override
	public String getErrorPath() {
		return "/error";
	}

	@RequestMapping("/error")
	public Object error() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (ctx == null) {
			return null;
		}

		ctx.getResponse().setCharacterEncoding("UTF-8");
		ctx.getResponse().setContentType("application/json;charset=UTF-8");

		if (ctx.getBoolean("flag")) {
			return handleOpenApiException();
		}

		return 504;
	}

	private Object handleOpenApiException() {
		String code = "999999";
		String message = "内部故障";
		String dataStr = "";
		String timestamp = String.valueOf(new Date().getTime());

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("rspCode", code);
		resultMap.put("rspMsg", message);
		resultMap.put("data", dataStr);
		resultMap.put("timestamp", timestamp);

		return resultMap;
	}

}
