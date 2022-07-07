package com.noob.easyexcel.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.noob.easyexcel.demo.CompanyEnumConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectCooperation extends BaseEntity {
    /**
     * 序号
     */
    @ExcelProperty(value = "序号", index = 1)
    private String serialNumber;
    /**
     * 公司名称
     */
    @ExcelProperty( index = 2, converter = CompanyEnumConverter.class)
    private String corporateName;
    /**
     * 项目余额
     */
    @ExcelProperty( index = 3)
    private String projectBalance;
}
