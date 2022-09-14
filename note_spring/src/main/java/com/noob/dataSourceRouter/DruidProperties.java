package com.noob.dataSourceRouter;

import com.alibaba.druid.filter.config.ConfigTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * druid 配置属性
 *
 * @author admin
 */
@Configuration
public class DruidProperties {
	@Value("${spring.datasource.druid.initialSize}")
	private int initialSize;

	@Value("${spring.datasource.druid.minIdle}")
	private int minIdle;

	@Value("${spring.datasource.druid.maxActive}")
	private int maxActive;

	@Value("${spring.datasource.druid.maxWait}")
	private int maxWait;

	@Value("${spring.datasource.druid.timeBetweenEvictionRunsMillis}")
	private int timeBetweenEvictionRunsMillis;

	@Value("${spring.datasource.druid.minEvictableIdleTimeMillis}")
	private int minEvictableIdleTimeMillis;

	@Value("${spring.datasource.druid.maxEvictableIdleTimeMillis}")
	private int maxEvictableIdleTimeMillis;

	@Value("${spring.datasource.druid.validationQuery}")
	private String validationQuery;

	@Value("${spring.datasource.druid.testWhileIdle}")
	private boolean testWhileIdle;

	@Value("${spring.datasource.druid.testOnBorrow}")
	private boolean testOnBorrow;

	@Value("${spring.datasource.druid.testOnReturn}")
	private boolean testOnReturn;

	@Value("${spring.datasource.druid.publicKey}")
	private String publicKey;

	public DruidDataSource dataSource(DruidDataSource datasource) {
		/** 配置初始化大小、最小、最大 */
		datasource.setInitialSize(initialSize);
		datasource.setMaxActive(maxActive);
		datasource.setMinIdle(minIdle);

		/** 配置获取连接等待超时的时间 */
		datasource.setMaxWait(maxWait);

		/** 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 */
		datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

		/** 配置一个连接在池中最小、最大生存的时间，单位是毫秒 */
		datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		datasource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);

		/**
		 * 用来检测连接是否有效的sql，要求是一个查询语句，常用select
		 * 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
		 */
		datasource.setValidationQuery(validationQuery);
		/**
		 * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		 */
		datasource.setTestWhileIdle(testWhileIdle);
		/** 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
		datasource.setTestOnBorrow(testOnBorrow);
		/** 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
		datasource.setTestOnReturn(testOnReturn);
		//oracle通过jdbc可获得表或字段的说明，仅在oracle数据库连接时有效
		datasource.addConnectionProperty("oracle.jdbc.remarks", "true");
		// 数据库密码使用加密
		datasource.addConnectionProperty("config.decrypt", "true");
		datasource.addConnectionProperty("config.decrypt.key", publicKey);
		return datasource;
	}


	public static void main(String args[]) throws Exception{
		ConfigTools.main(new String[]{"Bms@2019"});
	}
}
