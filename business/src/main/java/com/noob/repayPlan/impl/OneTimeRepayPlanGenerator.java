package com.noob.repayPlan.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.noob.repayPlan.AbstractRepayPlanGenerator;
import com.noob.repayPlan.LoanParam;
import com.noob.repayPlan.RepayPlan;

/**
 * 一次性还本付息还款方式
 * 
 */
public class OneTimeRepayPlanGenerator extends AbstractRepayPlanGenerator {
	@Override
	public List<RepayPlan> calculate(LoanParam loanDto) {
		Date endDate = loanDto.getEndDate();
		Date startDate = loanDto.getStartDate();
		int interestDays = calculateInterestDays(loanDto.isCalculateInterestFromNow(), startDate, endDate);
		int yearDays = loanDto.getRateBaseType() == RateBaseTypeEnum.DAYLY_360.getType() ? 360 : 365;
		BigDecimal amount = loanDto.getAmount();
		BigDecimal interest = calculateInterest(BigDecimal.valueOf(yearDays * 100), amount, loanDto.getYearRate(),
				loanDto.getInterestRoundingMode(), interestDays);

		return Arrays.asList(RepayPlan.init(loanDto.getLoanNo(), loanDto.getGraceDays(), 1, endDate, amount, interest,
				BigDecimal.ZERO));
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS001;
	}

	@Override
	public List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultDivBase) {
		return null;
	}

}
