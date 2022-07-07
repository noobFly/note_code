package com.noob.easyexcel.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.noob.easyexcel.demo.CompanyEnumConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreditBank extends BaseEntity {
    /**
     * 序号
     */
    @ExcelProperty(value = "序号", index = 1) // index从0开始
    private String serialNumber;
    /**
     * 公司名称
     */
    @ExcelProperty(index = 2, converter = CompanyEnumConverter.class)
    private String corporateName;

    /**
     * 授信额度
     */
    @ExcelProperty(index = 3)
    private java.math.BigDecimal credit;

    /**
     * 授信有效期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ExcelProperty(index = 4)
    @DateTimeFormat(value = "yyyy-MM-dd")
    private java.util.Date creditValidity;

}
