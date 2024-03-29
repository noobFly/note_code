
package com.noob.dataCheck;


import com.google.common.collect.Lists;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DataCheckTableMapping {
    //columns START
    /**
     * 编号
     */
    private Long id;
    /**
     * 主题： 1 产基项目
     */
    @NotNull(message = "表的类型不能为空")
    private Integer topic;
    /**
     * excel表
     */
    @NotBlank(message = "excel的sheet名不能为空")
    private String sheetName;
    /**
     * 关联的数据表
     */
    private String tableName;
    /**
     * 标题行 多个以逗号分开
     */
    @NotBlank(message = "标题行不能为空")
    private String headIndex;
    /**
     * 数据开始行
     */
    @NotNull(message = "数据开始行不能为空")
    private Integer dataStartIndex;
    /**
     * 创建者
     */
    private String createBy;
    /**
     * 创建时间
     */
    private java.util.Date createTime;
    /**
     * 更新者
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private java.util.Date updateTime;
    /**
     * 备注
     */
    private String remark;

    // 主键 -> 一定要选择字符串类型
    private List<DataCheckColumnMapping> primaryKeyColumns;

    @NotEmpty(message = "字段映射关系不能为空")
    private List<@Valid DataCheckColumnMapping> columnMappings;

    public List<String> getHeadIndexList() {
        return Lists.newArrayList(headIndex.split(","));
    }

}


