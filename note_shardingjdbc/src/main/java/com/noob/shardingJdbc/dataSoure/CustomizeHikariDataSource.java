package com.noob.shardingJdbc.dataSoure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.PropertyElf;

import java.util.Properties;

public class CustomizeHikariDataSource extends HikariDataSource {

	public CustomizeHikariDataSource() {
		super();
	}
	
	public CustomizeHikariDataSource(HikariConfig configuration) {
		super(configuration);
	}

	/**
	 * 解决sharding-jdbc在设置setDataSourceProperties后不生效的问题
	 */
	@Override
	public void setDataSourceProperties(Properties dsProperties) {
		super.setDataSourceProperties(dsProperties);
		PropertyElf.setTargetFromProperties(this, this.getDataSourceProperties());
	}
}
