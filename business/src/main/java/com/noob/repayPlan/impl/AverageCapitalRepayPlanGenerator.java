package com.noob.repayPlan.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.noob.repayPlan.AbstractRepayPlanGenerator;
import com.noob.repayPlan.LoanParam;
import com.noob.repayPlan.RepayPlan;

/**
 * 等额本金
 * <p>
 * 先均分本金， 按剩余本金计算每月利息 （首期是按日计息，余下的按月或日）
 * 
 */
public class AverageCapitalRepayPlanGenerator extends AbstractRepayPlanGenerator {

	@Override
	public List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultBasePeriods) {
		List<RepayPlan> planList = new ArrayList<>();
		int periodCount = periodEndDateMap.size(); // 总期数
		Date periodBeginDate = loanDto.getStartDate();
		BigDecimal amount = loanDto.getAmount();
		BigDecimal yearRate = loanDto.getYearRate();

		int curPeriod = 1;
		BigDecimal calculateAmount = amount; // 剩余计息本金

		BigDecimal periodCapital = amount.divide(BigDecimal.valueOf(periodCount), 2, loanDto.getCapitalRoundingMode()); // 每期应还本金

		for (Entry<Date, Boolean> dateEntry : periodEndDateMap.entrySet()) {
			Date periodEndDate = dateEntry.getKey();
			Boolean isDayRate = dateEntry.getValue();

			BigDecimal capital = curPeriod == periodCount ? calculateAmount : periodCapital;

			int realPeriods = isDayRate
					? calculateInterestDays(loanDto.isCalculateInterestFromNow(), periodBeginDate, periodEndDate)
					: 1;// 首期或指定 则用日利息计算。
			BigDecimal basePeriods = isDayRate && !RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType())
					? RateBaseTypeEnum.DAYLY_365.getBase()
					: defaultBasePeriods; // 首月按日计息基数365，其他月份则以入参为准

			BigDecimal interest = calculateInterest(basePeriods, calculateAmount, yearRate,
					loanDto.getInterestRoundingMode(), realPeriods);

			calculateAmount = calculateAmount.subtract(capital); // 剩余还款本金

			validate(interest, capital, calculateAmount);

			planList.add(RepayPlan.init(loanDto.getLoanNo(), loanDto.getGraceDays(), curPeriod, periodEndDate, capital,
					interest, calculateAmount));

			periodBeginDate = periodEndDate; // 下一期的起息日
			curPeriod++;
		}
		return planList;
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS003;
	}
}
