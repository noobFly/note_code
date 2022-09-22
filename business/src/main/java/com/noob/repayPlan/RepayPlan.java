
package com.noob.repayPlan;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RepayPlan {
	/** 主键 */
	private Long id;
	/** 借据号 */
	private String loanNo;
	/** 期号 */
	private Integer period;
	/** 到期日 */
	private Date shouldRepaymentDate;
	/** 本金 */
	private BigDecimal shouldRepaymentPrincipal;
	/** 正常利息 */
	private BigDecimal shouldRepaymentInterest = BigDecimal.ZERO;
	/** 逾期利息 */
	private BigDecimal shouldRepaymentPenalty = BigDecimal.ZERO;
	/** 应还费用 */
	private BigDecimal shouldRepaymentFee = BigDecimal.ZERO;
	/** 已还本金 */
	private BigDecimal actualRepaymentPrincipal = BigDecimal.ZERO;
	/** 已还利息 */
	private BigDecimal actualRepaymentInterest = BigDecimal.ZERO;
	/** 已还逾期利息 */
	private BigDecimal actualRepaymentPenalty = BigDecimal.ZERO;
	/** 已还费用 */
	private BigDecimal actualRepaymentFee = BigDecimal.ZERO;
	/** 利息减免金额 */
	private BigDecimal interestDeductionAmount = BigDecimal.ZERO;
	/** 罚息减免金额 */
	private BigDecimal penaltyDeductionAmount = BigDecimal.ZERO;
	/** 剩余本金 */
	private BigDecimal remainingPrincipal = BigDecimal.ZERO;
	/** 逾期标志 {@link LoanConst.isOverdue} */
	private Integer isOverdue;
	/** 最近还款日 */
	private Date actualRepaymentDate;
	/** 还款计划状态 {@link RepayPlanStatus} */
	private Integer repaymentPlanStatus;
	/** 宽限期天数 */
	private Integer graceDays;
	/** 更新版本 */
	private Integer version;
	/** 创建时间 */
	private Date createTime;
	/** 更新时间 */
	private Date updateTime;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public static RepayPlan init(String loanNo, Integer graceDays, Integer period, Date repayDate,
			BigDecimal capital, BigDecimal interest, BigDecimal remainingPrincipal) {
		RepayPlan plan = new RepayPlan();
		plan.setLoanNo(loanNo);
		plan.setShouldRepaymentPrincipal(capital);
		plan.setShouldRepaymentInterest(interest == null ? BigDecimal.ZERO : interest);
		plan.setShouldRepaymentDate(repayDate);
		plan.setRepaymentPlanStatus(1);// 未还款
		plan.setIsOverdue(0);// 未逾期
		plan.setPeriod(period);
		plan.setRemainingPrincipal(remainingPrincipal == null ? BigDecimal.ZERO : remainingPrincipal);
		plan.setGraceDays(graceDays == null ? 0 : graceDays);
		return plan;

	}
}
