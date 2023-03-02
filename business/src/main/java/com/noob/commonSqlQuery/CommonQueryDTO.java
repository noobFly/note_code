package com.noob.commonSqlQuery;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CommonQueryDTO {
    @NotNull(message = "主体不能为空")
    private Integer topic;
    private boolean startPage;
    @SqlValid
    private String orderBy;
    // 排序： up 升序 down 降序
    private String sortType;
    @Valid
    private List<@SqlValid @NotEmpty String> selectColumnList;
    @Valid
    private List<@NotNull QueryConditionDTO> filterConditionList;

    @Data
    public static class QueryConditionDTO {
        @SqlValid
        @NotBlank(message = "查询字段必能为空")
        private String column;
        @SqlValid
        private String value;
       // 类型(需要在mapper.xml文件里適配)： < 2；<= 3；> 4；  >= 5； 模糊匹配 6； 集合in 7 ; is not null 8 ; is null 9
        private int type;


    }

}

