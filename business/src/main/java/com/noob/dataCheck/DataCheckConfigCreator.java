package com.noob.dataCheck;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.text.CaseUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DataCheckConfigCreator {

    Map<Integer, Map<String, Method>> cache = new HashMap<>();
    // 表字段对应的方法名。符合驼峰规则
    Map<String, String> columnNameMap = new HashMap<>();
    // Class类的方法集合
    Map<Class, List<Method>> methodMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (DataCheckTopic element : DataCheckTopic.values()) {
            methodMap.put(element.cls, Arrays.asList(element.cls.getDeclaredMethods())); // 方法一定是public
        }
    }

    public void clear(String topic) {
        if (Strings.isNullOrEmpty(topic)) cache.remove(topic);
        else cache.clear();
    }

    public DataCheckConfig create(String topic, boolean clearCache) {

        if (clearCache) {
            clear(topic);
        }
        DataCheckConfig config = new DataCheckConfig();
        List<DataCheckTableMapping> tables = getTableConfig(topic);
        List<DataCheckColumnMapping> columns = getColumnConfig(topic);

        tables.stream().forEach(table -> {
            List<DataCheckColumnMapping> children = columns.stream().filter(t -> t.getTopic().equals(table.getTopic())).collect(Collectors.toList());
            // 将数据库字段映射为getter
            Integer tableTopic = table.getTopic();
            Map<String, Method> map = getMethodMap(children, tableTopic);
            children.stream().forEach(t -> t.setGetter(map.get(t.getColumnName())));
            table.setPrimaryKeyColumns(children.stream().filter(t -> "Y".equals(t.getPrimaryKey())).collect(Collectors.toList()));
            children.removeAll(table.getPrimaryKeyColumns());
            table.setColumnMappings(children);
        });
        config.setTableMappings(tables);
        return config;
    }


    private Map<String, Method> getMethodMap(List<DataCheckColumnMapping> children, Integer tableTopic) {
        return cache.computeIfAbsent(tableTopic, key -> {
            Map<String, Method> newMap = new HashMap<>();
            List<Method> methods = methodMap.get(DataCheckTopic.getByTopic(tableTopic).getCls());
            children.forEach(t -> newMap.put(t.getColumnName(), methods.stream().filter(a -> a.getName().equals(columnNameMap.computeIfAbsent(t.getColumnName(), columnName -> "get" + CaseUtils.toCamelCase(columnName, true, '_')))).findAny().get()));
            return newMap;
        });
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DataCheckTopic {
        PROJECT(1, "ft_project_collect", DataEntity.class, "项目信息");
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
    protected abstract List<DataCheckColumnMapping> getColumnConfig(String topic);

    // sheet-表 配置
    protected abstract List<DataCheckTableMapping> getTableConfig(String topic);

}
