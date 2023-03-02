package com.noob.commonSqlQuery;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CommonAdmMapper {
    List<Map<String, Object>> query(@Param("table") String table, @Param("sort") String sort, @Param("queryModel") CommonQueryDTO queryDTO);
}
