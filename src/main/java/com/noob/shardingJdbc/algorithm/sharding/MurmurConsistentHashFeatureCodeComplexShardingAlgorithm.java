package com.noob.shardingJdbc.algorithm.sharding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import com.noob.shardingJdbc.algorithm.config.FeatureCodeUtil;
import com.noob.shardingJdbc.algorithm.config.ShardingConst;
import com.noob.shardingJdbc.algorithm.sharding.support.MurmurShadingSelector;

public class MurmurConsistentHashFeatureCodeComplexShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {

    private final ShadingSelector shadingSelector;
    private final static String UNDERLINE = "_";

    private final static String LOAN_NO_COLUMN_NAME = "loan_no";
    private final static String CONTRACT_NO_COLUMN_NAME = "contract_no";

    public  MurmurConsistentHashFeatureCodeComplexShardingAlgorithm() {
        shadingSelector = new MurmurShadingSelector(ShardingListBuilder.build(ShardingConst.SHARDING_COUNT));
    }
    
    @Override
    public Collection<String> doSharding(Collection<String> tableNames, ComplexKeysShardingValue<String> complexKeysShardingValue) {
        Map<String, Collection<String>> columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        //sharding的字段名从sql中取，为了最大兼容，将字段名改为小写，再映射
        Map<String, Collection<String>> lowerCaseColumnNameAndShardingValuesMap = new HashMap<>();
        for(Iterator<Entry<String, Collection<String>>> it = columnNameAndShardingValuesMap.entrySet().iterator(); it.hasNext();) {
        	Entry<String, Collection<String>> entry = it.next();
        	lowerCaseColumnNameAndShardingValuesMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        
        List<String> featureCodeList = getFeatureCodeList(lowerCaseColumnNameAndShardingValuesMap, LOAN_NO_COLUMN_NAME);
        if(featureCodeList.isEmpty()) {
        	featureCodeList = getFeatureCodeList(lowerCaseColumnNameAndShardingValuesMap, CONTRACT_NO_COLUMN_NAME);
        }
        
        Set<String> shardings = new HashSet<>(); //去重
        
        String suffix;
        for(String featureCode : featureCodeList) {
        	suffix = UNDERLINE + shadingSelector.select(featureCode).getShadingCode();
            
        	boolean shardingExist = false;
            for (String tableName : tableNames) {
                if (tableName.endsWith(suffix)) {
                	shardings.add(tableName);
                	shardingExist = true;
                    break;
                }
            }
            if(shardingExist) {
            	continue;
            }
            String tableName = complexKeysShardingValue.getLogicTableName();
            throw new RuntimeException("Sharding-jdbc分表中未包括分表：" + tableName + suffix);
        }
        
        return shardings;
    }
    
    private List<String> getFeatureCodeList(Map<String, Collection<String>> map, String columnName){
    	Collection<String> values = map.get(columnName);
    	if(values == null || values.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	List<String> featureCodeList = new ArrayList<>();
    	for(String value : values) {
    		featureCodeList.add(getFeatureCode(value));
    	}
    	return featureCodeList;
    }

    private String getFeatureCode(String shardingColumn) {
        return FeatureCodeUtil.getFeatureCode(shardingColumn); //从第四位起3位特征码
    }
}
