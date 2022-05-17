package com.noob.Excel;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExcelDTO {

    @Excel(name = "属性1")
    private String attr1 = "attr1";
    @Excel(name = "属性2")
    private Date attr2 = new Date();
    @Excel(name = "属性3", suffix = "%")
    private double attr3 = 3.3389d;
    @Excel(name = "属性4")
    private int attr4 = 4;
    @Excel(name = "属性5", cellType = Excel.ColumnType.STRING)
    private int attr5 = 000005;
    @Excel(name = "属性6", cellType = Excel.ColumnType.NUMERIC)
    private int attr6 = 000006;
    @Excel(name = "属性7", readConverterExp = "0=男,1=女,2=未知")
    private int attr7 = 2;
    @Excel(name = "属性8", defaultValue = "属性8")
    private String attr8;
    @Excel(name = "属性9", dictType = "DICT_ENUM", sort = 1)
    private int attr9;
    @Excel(name = "属性13", scale = 2)
    private BigDecimal attr10 = new BigDecimal("0.1298");
    private int attr17 = 17;
}
