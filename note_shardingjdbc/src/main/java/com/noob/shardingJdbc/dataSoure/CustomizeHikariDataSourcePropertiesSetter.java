package com.noob.shardingJdbc.dataSoure;

import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetter;
import org.apache.shardingsphere.spring.boot.datasource.HikariDataSourcePropertiesSetter;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

public final class CustomizeHikariDataSourcePropertiesSetter implements DataSourcePropertiesSetter {

    HikariDataSourcePropertiesSetter setter = new HikariDataSourcePropertiesSetter();

    public void propertiesSet(final Environment environment, final String prefix, final String dataSourceName, final DataSource dataSource) {
        setter.propertiesSet(environment, prefix, dataSourceName, dataSource);
    }

    @Override
    public String getType() {
        return "com.noob.shardingJdbc.dataSoure.CustomizeHikariDataSource";
    }
}
