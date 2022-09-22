package com.noob.filter;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import lombok.extern.slf4j.Slf4j;

// 后置过滤
// 在ZuulServlet.service的处理逻辑里， 异常了要执行“error” -> "post" 类型的过滤器
@Component
@Slf4j
public class PostZuulFilter extends BaseZuulFilter {

	@Override
	public boolean shouldFilter() {
		System.out.println("PostZuulFilter shouldFilter");
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getBoolean("apiFlag") && ctx.sendZuulResponse();

	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		InputStream responseDataStream = ctx.getResponseDataStream();
		try {
			String msg = readAsString(responseDataStream, null);
			log.info("PostZuulFilter run  response: " + msg);
			ctx.getResponse().setCharacterEncoding("UTF-8");
		//	ctx.getResponse().setContentType("application/json;charset=UTF-8");
			ctx.setResponseBody(msg); // 需要自己从stream里解析数据设置到resposeBody
			ctx.setResponseStatusCode(200);
			ctx.setSendZuulResponse(false); // 不会被后面的过滤器再处理了
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.POST_TYPE;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
