package com.noob.shardingJdbc.algorithm.sharding;

/**
 * 分片选择器
 *
 */
public interface ShadingSelector {
	/**
	 * 选择分片
	 * @param key 分片键
	 * @return
	 */
	public Sharding select(String key);
}
