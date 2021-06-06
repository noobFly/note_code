package com.noob.controller.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import lombok.extern.slf4j.Slf4j;

// 前置过滤. 外部访问： http://localhost:5555/api/server/log?msg=18
@Slf4j
@Component
public class PreZuulFilter extends BaseZuulFilter {

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String uri = ctx.getRequest().getRequestURI();
		log.info("PreZuulFilter shouldFilter " + uri);
		return uri.startsWith("/api"); // 以url来判定由哪组过滤器处理
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.set("apiFlag", true); // 可以在真实路由前，做数据报文的加解密、白名单校验、权限控制、流量控制 等打标操作
		log.info("PreZuulFilter run");
		String requestURI = ctx.getRequest().getRequestURI();
		ctx.put(FilterConstants.REQUEST_URI_KEY, requestURI.substring(5));// 因为测试时没有给业务server设置context-name,简单处理一下.
		// 但是这里会被之后执行的PreDecorationFilter处理回原来的url,所以要放到route类型的filter里去！！
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
