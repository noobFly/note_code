package com.noob.ipLimit;

import cn.hutool.core.lang.Assert;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求速度受限的查询器，避免请求过于频繁被服务器拒绝
 *
 */
@Slf4j
public abstract class AbstractIpLocationLimitRateQuerier extends AbstractIpLocationQuerier {
	private final RateLimiter rateLimiter;
	
	/**
	 * 
	 * @param permitsPerSecond 每秒允许的请求数
	 */
	public AbstractIpLocationLimitRateQuerier(long permitsPerSecond) {
		Assert.isTrue(permitsPerSecond > 0, "permitsPerSecond必须大于0");
		rateLimiter = RateLimiter.create(permitsPerSecond);
	}
	
	@Override
	public String[] get(String ipAddr) {
		if(!rateLimiter.tryAcquire()) { //立刻获取一个令牌
			return null;
		}
		try {
			return query(ipAddr);
		} catch (Exception e) {
			log.error("IP所在地查询异常", e);
		}
		return null;
	}

}
