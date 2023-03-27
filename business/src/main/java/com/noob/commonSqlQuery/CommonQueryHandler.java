package com.noob.commonSqlQuery;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public class CommonQueryHandler {
    @Resource
    CommonMapper commonMapper;

    public List<Map<String, Object>> queryAdm(CommonQueryDTO queryDTO) {
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
        activity_industry_fund(2, "产业基金", new CommonQueryDTO.QueryCondition("tag1", "明细"), true),
        direct_project_total(3, "创投项目金额统计", null, false);
        private int type;
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
