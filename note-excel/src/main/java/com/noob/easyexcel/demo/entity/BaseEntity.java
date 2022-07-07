package com.noob.easyexcel.demo.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public abstract class BaseEntity {
    @ExcelIgnore
    private Long id;

    /**
     * 数据月份
     */
    @ExcelIgnore
    private String month;
    /**
     * 创建时间
     */
    @ExcelIgnore // 不在excel里的字段要使用该注解，不然会默认在解析过程里按顺序对应excel的column_index
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private java.util.Date createTime;

    // 为了兼容不同业务的excel读入起始列不同，判定数据为空的依据
    public abstract String getSerialNumber();
    public abstract void setSerialNumber(String no);

}
