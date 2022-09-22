package com.noob.dataSourceRouter;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源。 <p>通过拦截器DataSourceAspect对执行线程切换数据源（这里一般是在sevice层）， 需要指定默认数据源。</p>
 * <p>另外的方式： 配置多个@MapperScan扫描的在不同的扫描路径下的mapper,指定定sqlSessionFactoryRef、 sqlSessionTemplateRef 。实例化多个SqlSessionFactoryBean分别指定不同的DataSource</p>
 */
public class DynamicDataSource extends AbstractRoutingDataSource
{
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey()
    {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
}