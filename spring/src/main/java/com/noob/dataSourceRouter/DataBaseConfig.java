package com.noob.dataSourceRouter;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import com.noob.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 *
 */
@Configuration
public class DataBaseConfig {
	@Bean
	@ConfigurationProperties("spring.datasource.druid.master")
	public DataSource masterDataSource(DruidProperties druidProperties) {
		DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
		return druidProperties.dataSource(dataSource);
	}

	@Bean
	@ConfigurationProperties("spring.datasource.druid.slave")
	@ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
	public DataSource slaveDataSource(DruidProperties druidProperties) {
		DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
		return druidProperties.dataSource(dataSource);
	}

	@Bean(name = "dynamicDataSource")
	@Primary
	public DynamicDataSource dataSource(DataSource masterDataSource) {
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put(DataSourceChoose.DataSourceType.MASTER.name(), masterDataSource);
		setDataSource(targetDataSources, DataSourceChoose.DataSourceType.SLAVE.name(), "slaveDataSource");
		return new DynamicDataSource(masterDataSource, targetDataSources); //指定default服务器
	}

	/**
	 * 设置数据源
	 * 
	 * @param targetDataSources 备选数据源集合
	 * @param sourceName        数据源名称
	 * @param beanName          bean名称
	 */
	public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName) {
		try {
			DataSource dataSource = SpringUtil.getBean(beanName);
			targetDataSources.put(sourceName, dataSource);
		} catch (Exception e) {
		}
	}

	/**
	 * DruidDataSoure 启动监控视图 https://mp.weixin.qq.com/s/VYk5_kcWPLNCyEpTkx4YuQ
	 * 去除监控页面底部的广告
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	@ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
	public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties) {
		// 获取web监控页面的参数
		DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
		// 提取common.js的配置路径
		String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
		String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
		final String filePath = "support/http/resources/js/common.js";
		// 创建filter进行过滤
		Filter filter = new Filter() {
			@Override
			public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				chain.doFilter(request, response);
				// 重置缓冲区，响应头不会被重置
//                response.resetBuffer();
				// 获取common.js
				String text = Utils.readFromResource(filePath);
				// 正则替换banner, 除去底部的广告信息
				text = text.replaceAll("<a.*?banner\"></a><br/>", "");
				text = text.replaceAll("powered.*?shrek.wang</a>", "");
				response.getWriter().write(text);
			}

			@Override
			public void destroy() {
			}
		};
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns(commonJsPattern);
		return registrationBean;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DynamicDataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
