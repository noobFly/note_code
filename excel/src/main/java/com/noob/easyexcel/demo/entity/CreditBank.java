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
    @com.alibaba.excel.annotation.format.NumberFormat(value = "0.00") // 导出格式化
    private java.math.BigDecimal credit;

    /**
     * 授信有效期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd",  timezone = "GMT+8") // springmvc响应时输出的对应格式的日期字符串. 指定时区否则可能有问题
    @ExcelProperty(index = 4)
    @DateTimeFormat(value = "yyyy-MM-dd") // 指定excel里日期格式
    private java.util.Date creditValidity;

}
