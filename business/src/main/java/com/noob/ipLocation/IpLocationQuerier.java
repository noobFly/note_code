package com.noob.ipLocation;

/**
 * IP所在地查询器
 *
 */
public interface IpLocationQuerier {

	/**
	 * 查询，直接返回，如果查询失败，返回null
	 * @param ipAddr
	 * @return [0] 省名称，[1] 市名称
	 */
	String[] get(String ipAddr);

}
