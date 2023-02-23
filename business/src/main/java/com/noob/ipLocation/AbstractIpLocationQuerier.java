package com.noob.ipLocation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractIpLocationQuerier implements IpLocationQuerier {

	/**
	 * 查询
	 * @param ipAddr
	 * @return
	 * @throws Exception
	 */
	protected abstract String[] query(String ipAddr) throws Exception;

	@Override
	public String[] get(String ipAddr) {
		try {
			return query(ipAddr);
		} catch (Exception e) {
			log.error("IP所在地查询异常", e);
		}
		return null;
	}

}
