package com.noob.commonSqlQuery;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 因为有sql注入风险所以在入参DTO的属性上都加了sql防御的校验器。
 * 本意是想支持通道式的通用查询。但如果查询条件比较复杂就无法支持！而且前端还得注意属性值的写法。
 * <p> 如果对查询字段使用了函数，返回出来的数据key也会带上函数...</>
 * 鸡肋!!!! 不建议使用。
 * <p>
         {
         "type": 3,
         "startPage": false,
         "orderBy": "fund_name",
         "orderSort": "asc",
         "selectColumnList": [
         "fund_name"
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
        String sortType = queryDTO.getSortType();
        String sort = Strings.isNullOrEmpty(sortType) ? null : ("up".equals(sortType) ? "asc" : "desc");
        return commonAdmMapper.query(table,sort, queryDTO);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum TableEnum {
        view_plt_fund_activity_details(1),
        view_plt_project_activity_details(2),
        view_project_invest_all(3);
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
