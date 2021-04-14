package com.noob.shardingJdbc.algorithm.sharding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ShardingListBuilder {
	private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00");

	public static List<Sharding> build(int shardingSize){
		List<Sharding> list = new ArrayList<>(shardingSize);
		for(int i = 0; i < shardingSize; i++) {
			list.add(new Sharding(DECIMAL_FORMAT.format(i)));
		}
		return list;
	}
	
	public static List<Sharding> build(int shardingSize, String logicTableName){
		List<Sharding> list = new ArrayList<>(shardingSize);
		for(int i = 0; i < shardingSize; i++) {
			list.add(new Sharding(DECIMAL_FORMAT.format(i), logicTableName));
		}
		return list;
	}
}
