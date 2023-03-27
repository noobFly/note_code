package com.noob.dataCheck;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.text.CaseUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>如果是使用每个库表对应的实体来获取数据则需要提前预设Method的映射; 就需要有#clear方法
 * <p>但如果用通用查询通道方式则不用, 它返回的Map<String,Object>的key就是表字段
 */
public abstract class DataCheckConfigCreator {

    Map<Integer, Map<String, Method>> cache = new HashMap<>();
    // 表字段对应的方法名。符合驼峰规则
    Map<String, String> columnNameMap = new HashMap<>();
    // Class类的方法集合 (如果用通用查询通道方式就不需要映射)
    Map<Class, List<Method>> methodMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (DataCheckTopic element : DataCheckTopic.values()) {
            methodMap.put(element.cls, Arrays.asList(element.cls.getDeclaredMethods())); // 方法一定是public
        }
    }

    public void clear(Integer topic) {
        if (topic != null) cache.remove(topic);
        else cache.clear();
    }

    public DataCheckTableMapping create(Integer topic, boolean clearCache) {

        if (clearCache) {
            clear(topic);
        }

        return create(topic);
    }

    public DataCheckTableMapping create(Integer topic) {
        DataCheckTableMapping table = getTableConfig(topic);
        if (table == null) {
            throw new RuntimeException("无稽查系统表配置");
        }

        List<DataCheckColumnMapping> columns = getColumnConfig(topic);
        if (table == null) {
            throw new RuntimeException(String.format("无稽查系统表%s的字段配置", table.getTableName()));
        }
        List<DataCheckColumnMapping> children = columns.stream().filter(t -> t.getTopic().equals(table.getTopic())).collect(Collectors.toList());
        Integer tableTopic = table.getTopic();
        Map<String, Method> map = getMethodMap(children, tableTopic);
        // 将数据库字段映射为getter
        children.stream().forEach(t -> t.setGetter(map.get(t.getColumnName())));
        table.setPrimaryKeyColumns(children.stream().filter(t -> "Y".equals(t.getPrimaryKey())).collect(Collectors.toList()));

        if (CollectionUtils.isEmpty(table.getPrimaryKeyColumns())) {
            throw new RuntimeException(String.format("无稽查系统表%s的字段主键配置", table.getTableName()));
        }
        children.removeAll(table.getPrimaryKeyColumns());
        table.setColumnMappings(children);
        return table;
    }


    private Map<String, Method> getMethodMap(List<DataCheckColumnMapping> children, Integer tableTopic) {
        return cache.computeIfAbsent(tableTopic, key -> {
            Map<String, Method> newMap = new HashMap<>();
            List<Method> methods = methodMap.get(DataCheckTopic.getByTopic(tableTopic).getCls());
            children.forEach(t -> newMap.put(t.getColumnName(), methods.stream().filter(a -> a.getName().equals(columnNameMap.computeIfAbsent(t.getColumnName(), columnName -> "get" + CaseUtils.toCamelCase(columnName, true, '_')))).findAny().get()));
            return newMap;
        });
    }

    // 如果使用通用查询通道方式则和CommonQueryHandler.TableEnum合并
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DataCheckTopic {
        PROJECT(1, "project_collect", DataEntity.class, "项目信息");
        private int topic;
        private String table; //用来页面指定查询表的字段明细
        private Class cls;
        private String msg;

        public static DataCheckTopic getByTopic(Integer topic) {
            for (DataCheckTopic element : DataCheckTopic.values()) {
                if (element.getTopic() == topic) return element;
            }
            return null;
        }
    }


    // 列-字段 配置
    protected abstract List<DataCheckColumnMapping> getColumnConfig(Integer topic);

    // sheet-表 配置
    protected abstract DataCheckTableMapping getTableConfig(Integer topic);

}
