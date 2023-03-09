package com.noob.commonSqlQuery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 因为有sql注入风险所以在入参DTO的属性上都加了sql防御的校验器。
 * 本意是想支持通道式的通用查询。
 * <p>
 *   缺点:
 *    1、多表联合查询等比较复杂的操作无法支持！
 *    2、前端得注意属性值的写法。
 *    3、有sql注入的风险
 *    4、因为ibatis的默认实现：在PreparedStatementHandler#execute 执行完sql后 在DefaultResultSetHandler#handleResultSets-> #applyAutomaticMappings 根据字段属性映射给返回对象实例的属性setter值时，如果为null不操作
 *       所以当用Map(默认HashMap)做返回对象类型时，会出现: 如果值为空，map里会没该key!
 *
 * </p>
 *
 * <p>
     {
     "type": 3,
     "startPage": false,
     "sortConditionList": [
     {
     "order": "fund_name",
     "sort": "desc"
     },
     {
     "order": "fund_name",
     "sort": "asc"
     }
     ],
     "selectColumnList": [
     "substr(fund_name,2)"
     ],
     "filterConditionList": [
     {
     "column": "fund_name",
     "value": "'中山贝森医疗产业投资企业（有限合伙）'",
     "type": "7"
     }
     ]
     }
 * </>
 */
public class CommonQueryHandler {
    @Resource
    CommonAdmMapper commonAdmMapper;

    public List<Map<String, Object>> queryAdm(CommonQueryDTO queryDTO) {
        String table = TableEnum.findTable(queryDTO.getTopic());
        return commonAdmMapper.query(table, queryDTO);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum TableEnum {
        view_plt_fund_activity_details(1),
        view_plt_project_activity_details(2);
        private int topic;

        public static String findTable(int topic) {
            for (TableEnum element : TableEnum.values()) {
                if (element.topic == topic) {
                    return element.name();
                }
            }
            return null;
        }
    }

}
