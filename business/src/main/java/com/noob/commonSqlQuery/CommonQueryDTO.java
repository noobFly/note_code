package com.noob.commonSqlQuery;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class CommonQueryDTO {
    @NotNull(message = "类型不能为空")
    //查询的表
    private Integer type;
    // 是否开启分页
    private boolean startPage;
    //排序字段
    public List<@Valid SortCondition> sortConditionList;
    // 分组字段
    public List<@SqlValid @NotEmpty String> groupColumnList;
    // 需要查询的字段
    private List<@SqlValid @NotEmpty String> selectColumnList;
    //过滤条件
    @Valid
    private List<QueryCondition> filterConditionList;

    // 因为有些表不仅仅是做了页面展示，也做了报表统计数。所以有一些默认的筛选条件
    private boolean mergeDefaultFilterCondition = false;

    @Data
    public static class SortCondition {

        @SqlValid
        @NotBlank(message = "排序字段不能为空")
        private String order;
        // 排序： asc 升序 , desc 降序
        @Pattern(regexp = "desc|asc")
        private String sort = "asc";
    }

    @Data
    @NoArgsConstructor
    public static class QueryCondition {
        @SqlValid
        @NotBlank(message = "查询字段必能为空")
        private String column;
        //字段属性值
        @SqlValid
        private String value;
        //(需要在mapper.xml里映射) 类型：(默认是=) < 2；<= 3；> 4；  >= 5； 模糊匹配 6； 集合in 7 ; is not null 8 ; is null 9
        private int type;

        public QueryCondition(String column, String value) {
            this.column = column;
            this.value = value;
        }

    }

}

