package com.noob.commonSqlQuery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Data
public class CommonQueryDTO {
    @NotNull(message = "类型不能为空")
    @ApiModelProperty(value = "查询的表")
    //查询的表
    private Integer type;
    // 是否开启分页
    @ApiModelProperty(value = "是否开启分页")
    private boolean startPage;
    //排序字段
    @ApiModelProperty(value = "排序字段")
    public List<@Valid SortCondition> sortConditionList;
    // 分组字段
    @ApiModelProperty(value = "分组字段")
    public List<@SqlValid(message="分组字段 not valid !") @NotEmpty String> groupColumnList;
    // 需要查询的字段
    @ApiModelProperty(value = "需要查询的字段")
    private List<@SqlValid(message="查询字段 not valid !") @NotEmpty String> selectColumnList;
    //过滤条件
    @Valid
    @ApiModelProperty(value = "过滤条件")
    private List<QueryCondition> filterConditionList;

    // 因为有些表不仅仅是做了页面展示，也做了报表统计数。所以有一些默认的筛选条件
    private boolean mergeDefaultFilterCondition = false;

    @ApiModelProperty(value = "额外信息")
    private Map<String, String> extraInfoMap = Maps.newHashMap();

    @Data
    public static class SortCondition {

        @SqlValid(message="排序字段 not valid !")
        @NotBlank(message = "排序字段不能为空")
        @ApiModelProperty(value = "排序字段")
        private String order;
        // 排序： asc 升序 , desc 降序
        @Pattern(regexp = "desc|asc")
        @ApiModelProperty(value = "排序： asc 升序 , desc 降序 ")
        private String sort = "asc";
    }

    @Data
    @NoArgsConstructor
    public static class QueryCondition {
        @SqlValid(message="过滤字段 not valid !")
        @NotBlank(message = "过滤字段能为空")
        @ApiModelProperty(value = "字段名")
        private String column;
        //字段属性值 如果type=7 , value 要传List
        @ApiModelProperty(value = "字段属性值")
        private Object value;
        //(需要在mapper.xml里映射) 类型：(默认是=) < 2；<= 3；> 4；  >= 5； 模糊匹配 6； 集合in 7 ; is not null 8 ; is null 9
        @ApiModelProperty(value = "类型：(默认是=) < 2；<= 3；> 4；  >= 5； 模糊匹配 6； 集合in 7 ; is not null 8 ; is null 9 ")
        private int type;

        public QueryCondition(String column, String value) {
            this.column = column;
            this.value = value;
        }

    }  public CommonQueryDTO countQueryDTO() {
        CommonQueryDTO dto  = new CommonQueryDTO();
        dto.setType(this.getType());
        dto.setSelectColumnList(Lists.newArrayList("count(*) as total"));
        dto.setGroupColumnList(this.getGroupColumnList());
        dto.setFilterConditionList(this.getFilterConditionList());
        dto.setMergeDefaultFilterCondition(this.isMergeDefaultFilterCondition());
        return dto;
    }

}

