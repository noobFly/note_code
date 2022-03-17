package com.noob.shardingJdbc.dataSoure;

import lombok.SneakyThrows;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

public final class HikariDataSourcePropertiesSetter implements DataSourcePropertiesSetter {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    @SneakyThrows(ReflectiveOperationException.class)
    public void propertiesSet(final Environment environment, final String prefix, final String dataSourceName, final DataSource dataSource) {
        Properties properties = new Properties();
        String datasourcePropertiesKey = prefix + dataSourceName.trim() + ".data-source-properties";
        if (PropertyUtil.containPropertyPrefix(environment, datasourcePropertiesKey)) {
            Map datasourceProperties = PropertyUtil.handle(environment, datasourcePropertiesKey, Map.class);
            properties.putAll(datasourceProperties);
            Method method = dataSource.getClass().getMethod("setDataSourceProperties", Properties.class);
            method.invoke(dataSource, properties);
        }
    }
    
    @Override
    public String getType() {
        return "cn.utrust.fintech.cbms.domain.config.CbmsHikariDataSource";
    }
}
