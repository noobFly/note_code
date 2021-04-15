package com.noob.repayPlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.noob.repayPlan.RepayPlanGenerator.RateBaseTypeEnum;
import com.noob.util.TimeUtil;

import lombok.Data;

@Data
public class LoanParam {
	/**
	 * 借据号
	 */
	protected String loanNo;
	/**
	 * 借款金额
	 */
	protected BigDecimal amount;
	/**
	 * 年化利率
	 */
	protected BigDecimal yearRate;
	/** 宽限期天数 */
	private Integer graceDays;
	/**
	 * 周期最小天数 范围默认在 [15, 25]
	 */
	protected int periodMinDay;

	/**
	 * 设置还款日，有效：1-28，值为0则表示不相关.
	 * <p>
	 * 在按期还款中配合periodMinDay生效。
	 * <p>
	 * 即使选择了按月计息，但指定了还款日, 首期依然按日计息，余下的期数按指定计息方式
	 */
	protected int repaymentDay;
	/**
	 * 起息日 YYYY-MM-DD
	 */
	@JSONField(format = "yyyy-MM-dd")
	protected Date startDate;
	/**
	 * 截息日 YYYY-MM-DD （周期性还款非必传，内部计算。一次性还款付息必填）
	 */
	@JSONField(format = "yyyy-MM-dd")
	protected Date endDate;

	/**
	 * 总期数 （一次性还款付息非必传，周期性还款必填）
	 */
	protected int totalPeriod;
	/**
	 * 设置计息基数，0: 对于按期借款默认按月 1:按日360 2:按日365<br>
	 * 
	 * {@link RateBaseTypeEnum}
	 */
	protected int RateBaseType;
	/**
	 * 等本等息 及 先息后本 是否需要在末期补利息差额
	 * 
	 */
	protected boolean endComplementInterest = true;
	/**
	 * 按日利息实际计息天数是否含当天 n or n+1 ; 默认false标识n+1
	 */
	protected boolean calculateInterestFromNow = false;
	/**
	 * 计算本金的厘位取舍方式，默认为RoundingMode.DOWN
	 */
	protected RoundingMode capitalRoundingMode = RoundingMode.HALF_UP;
	/**
	 * 计算利息的厘位取舍方式，默认为RoundingMode.DOWN
	 */
	protected RoundingMode interestRoundingMode = RoundingMode.HALF_UP;
	/**
	 * 单期还款金额取舍方式
	 */
	protected RoundingMode periodAmountRoundingMode = RoundingMode.HALF_UP;

	public void checkParams() {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("金额不能为空或小于0");
		}

		if (yearRate == null || yearRate.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("利率不能为空或小于0");
		}

		if (startDate == null) {
			throw new IllegalArgumentException("起息日不能为空");
		}

		if (endDate != null && startDate.after(endDate)) {
			throw new IllegalArgumentException("截息日不能早于起息日");
		}

		if ((RateBaseType < RateBaseTypeEnum.MONTH.getType() || RateBaseType > RateBaseTypeEnum.DAYLY_365.getType())) {
			throw new IllegalArgumentException("计息基数值不正确");
		}
		// 限制在28 是因为Calendar.set Calendar.DAY_OF_MONTH 时，若是一个超出当月天数的数值，将累加天数顺延至下月
		if (repaymentDay < 0 || repaymentDay > 28) {
			throw new IllegalArgumentException("还款日不正确，应为0-28");
		}

		if (periodMinDay < 0 || periodMinDay > 25 || (periodMinDay > 0 && periodMinDay < 10)) {
			throw new IllegalArgumentException("periodMinDay不正确，应为10-25");
		}
	}

	public void setStartDate(Date startDate) {
		this.startDate = TimeUtil.getDateBegin(startDate);
	}

	public void setEndDate(Date endDate) {
		this.endDate = TimeUtil.getDateBegin(endDate);
	}

	public static void main(String[] args) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = df.parse("2019-02-01");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 29); // 还款日
		System.out.println(df.format(cal.getTime())); // 2019-03-01 若是一个超出当月天数的数值，将累加天数顺延至下月

	}

}
