package com.noob.shardingJdbc.algorithm.sharding;

import com.google.common.base.Strings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片
 *
 */
@Data
public class Sharding {
	/**
	 * 分片编号，即为分片后缀
	 */
	private String shadingCode;
	
	private String logicTableName;
	
	public Sharding(String shadingCode) {
		this.shadingCode = shadingCode;
	}

	public Sharding(String shadingCode, String logicTableName) {
		this.shadingCode = shadingCode;
		this.logicTableName = logicTableName;
	}
	
	public String getPhysicalTableName() {
		if(Strings.isNullOrEmpty(shadingCode)) {
			throw new RuntimeException("未设置分片编码");
		}
		
		if(Strings.isNullOrEmpty(logicTableName)) {
			throw new RuntimeException("未设置逻辑表名");
		}
		
		return logicTableName + "_" + shadingCode;
	}
}
