package com.noob.shardingJdbc.algorithm.sharding;

import java.util.Collection;
import java.util.Iterator;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import com.noob.shardingJdbc.algorithm.config.ShardingConst;
import com.noob.shardingJdbc.algorithm.sharding.support.MurmurShadingSelector;

/**
 * Murmur一致性hash分表算法
 *
 */
public class MurmurConsistentHashShardingAlgorithm implements PreciseShardingAlgorithm<String> {
	private final ShadingSelector shadingSelector;
	private final static String UNDERLINE = "_";
	
	public  MurmurConsistentHashShardingAlgorithm() {
		shadingSelector = new MurmurShadingSelector(ShardingListBuilder.build(ShardingConst.SHARDING_COUNT));
	}

	@Override
	public String doSharding(Collection<String> tableNames, PreciseShardingValue<String> shardingValue) {
		String suffix = UNDERLINE + shadingSelector.select(shardingValue.getValue()).getShadingCode();
		
		for(Iterator<String> it = tableNames.iterator(); it.hasNext();) {
			String tableName = it.next();
			if(tableName.endsWith(suffix)) {
				return tableName;
			}
		}
		
		//发现物理表未在sharding-jdbc的分片列表中很重要
		String tableName = shardingValue.getLogicTableName();		
		throw new RuntimeException("Sharding-jdbc分表中未包括分表：" + tableName + suffix);
	}
}
