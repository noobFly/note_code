package com.noob.commonSqlQuery;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public class CommonQueryHandler {
    @Resource
    CommonMapper commonMapper;
    public Long count(CommonQueryDTO queryDTO) {
        List<Map<String, Object>> result = this.query(queryDTO.countQueryDTO());
        return CollectionUtils.isNotEmpty(result) && result.get(0) != null && result.get(0).get("total") != null ? (Long) result.get(0).get("total") : 0;
    }

    public List<Map<String, Object>> query(CommonQueryDTO queryDTO) {
        TableEnum table = TableEnum.findTable(queryDTO.getType());
        if (queryDTO.isMergeDefaultFilterCondition()) {
            CommonQueryDTO.QueryCondition extraFilterCondition = table.getExtraFilterCondition();
            if (extraFilterCondition != null) {
                List<CommonQueryDTO.QueryCondition> filterConditionList = queryDTO.getFilterConditionList();
                if (filterConditionList == null) {
                    filterConditionList = Lists.newArrayList();
                }
                filterConditionList.add(table.getExtraFilterCondition());
            }
        }
        return commonMapper.query(table.name(), queryDTO);
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT) // enum默认jackson输出到前端是只有名字的! 加上后能转成object输出
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum TableEnum {
        fund_activity_details(1, null, null, false),
        activity_industry_fund(2, "基金", new CommonQueryDTO.QueryCondition("tag1", "明细"), true),
        direct_project_total(3, "创投金额统计", null, false);
        private int type;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 1.9之后： 在Setter方法上加@Jsonignore会导致整个这个属性在序列化过程中被忽略。所以： 通过设置JsonProperty的access属性来确定当前属性是不是需要自动序列化/反序列化
        private String msg;
        private CommonQueryDTO.QueryCondition extraFilterCondition;
        // 是否需要核对
        private boolean needCheck;

        public static TableEnum findTable(int type) {
            for (TableEnum element : TableEnum.values()) {
                if (element.type == type) {
                    return element;
                }
            }
            return null;
        }
    }


}
