package com.noob.dataCheck;

import lombok.Data;

@Data
public class DataEntity {

    /**
     * 基金名称
     */
    private String fundName;

    /**
     * 所投项目公司名称
     */
    private String projectName;
    /**
     * 项目所属行业领域（一级目录）
     */
    private String industry1st;

    /**
     * 投资时间
     */
    private String investDate;
    /**
     * 最新持股比例
     */
    private String lastShareholdingRatio;
    /**
     * 签约投资金额
     */
    private java.math.BigDecimal signAmount;

    /**
     * 员工跟投额
     */
    private java.math.BigDecimal followInvestAmount;
    /**
     * 实际退出日期
     */
    private String exitDate;
}
