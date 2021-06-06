package com.noob.controller.filter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import lombok.extern.slf4j.Slf4j;

/**
 * 路由过滤器。 按filterOrder由小到大执行
 * <p>
 * RibbonRoutingFilter -> filterOrder = 10 : shouldFilter条件是： 一定不能指定routeHost,
 * 且一定要指定下游服务的serverId.
 * <p>
 * (ctx.getRouteHost() == null && ctx.get(SERVICE_ID_KEY) != null
 * <p>
 * SimpleHostRoutingFilter -> filterOrder = 100 shouldFilter条件是： 一定指定routeHost!
 *
 */
@Slf4j
@Component
public class RouteZuulFilter extends BaseZuulFilter {
	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		System.out.println("RouteZuulFilter shouldFilter");

		return ctx.getBoolean("apiFlag") && ctx.sendZuulResponse();
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.put(FilterConstants.REQUEST_URI_KEY, ctx.getRequest().getRequestURI().substring(4));
		log.info("RouteZuulFilter run serviceId:" + ctx.get(SERVICE_ID_KEY));

		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.ROUTE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
