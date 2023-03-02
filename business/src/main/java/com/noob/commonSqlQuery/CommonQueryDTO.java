package com.noob.commonSqlQuery;

import lombok.Data;

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
    private Integer topic;
    // 是否开启分页
    private boolean startPage;
    //排序字段
    public List<@Valid SortCondition> sortConditionList;
    // 需要查询的字段
    private List<@SqlValid @NotEmpty String> selectColumnList;
    //过滤条件
    @Valid
    private List<QueryCondition> filterConditionList;

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
    public static class QueryCondition {
        @SqlValid
        @NotBlank(message = "查询字段必能为空")
        private String column;
        //字段属性值
        @SqlValid
        private String value;
        //(需要在mapper.xml里映射) 类型： < 2；<= 3；> 4；  >= 5； 模糊匹配 6； 集合in 7 ; is not null 8 ; is null 9
        private int type;


    }

}

