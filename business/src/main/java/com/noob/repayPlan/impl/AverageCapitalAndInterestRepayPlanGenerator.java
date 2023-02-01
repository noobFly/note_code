package com.noob.repayPlan.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.noob.repayPlan.AbstractRepayPlanGenerator;
import com.noob.repayPlan.LoanParam;
import com.noob.repayPlan.RepayPlan;

/**
 * 等本等息
 * <p>
 * 与等额本金基本相同，唯一区别是计算利息时，本金始终等于总本金。 每月可按月或按日计算利息
 * <p>
 * 最终利息与一次性还本付息、先息后本的利息额相同（分期会因为每期尾数抛弃而略有差异）
 * <p>
 * 
 */

public class AverageCapitalAndInterestRepayPlanGenerator extends AbstractRepayPlanGenerator {

	@Override
	public List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultBasePeriods) {
		List<RepayPlan> planList = new ArrayList<>();
		int periodCount = periodEndDateMap.size(); // 总期数
		Date periodBeginDate = loanDto.getStartDate();
		BigDecimal amount = loanDto.getAmount();
		BigDecimal yearRate = loanDto.getYearRate();
		RoundingMode interestRoundingMode = loanDto.getInterestRoundingMode();

		int curPeriod = 1;
		BigDecimal calculateAmount = amount; // 剩余计息本金
		BigDecimal distributedInterest = BigDecimal.ZERO; // 已计利息

		BigDecimal periodCapital = amount.divide(BigDecimal.valueOf(periodCount), 2, loanDto.getCapitalRoundingMode()); // 每期应还本金

		for (Entry<Date, Boolean> dateEntry : periodEndDateMap.entrySet()) {
			Date periodEndDate = dateEntry.getKey();
			Boolean isDayRate = dateEntry.getValue();

			int realPeriods = isDayRate
					? calculateInterestDays(loanDto.isCalculateInterestFromNow(), periodBeginDate, periodEndDate)
					: 1;// 首期或指定 则用日利息计算。
			BigDecimal basePeriods = isDayRate && !RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType())
					? RateBaseTypeEnum.DAYLY_365.getBase()
					: defaultBasePeriods; // 主要是应对首个还款周期超出1个月(期)的情况下按日计息，如果没有指定计息基数具体日则默认365

			BigDecimal capital = BigDecimal.ZERO;
			BigDecimal interest = BigDecimal.ZERO;
			if (curPeriod == periodCount) { // 最后一期
				capital = calculateAmount;
				if (loanDto.isEndComplementInterest()) {
					int totalBaseCount = calculateInterestDays(loanDto.isCalculateInterestFromNow(),
							loanDto.getStartDate(), loanDto.getEndDate());
					BigDecimal totalInterest = calculateInterest(
							RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType()) ? defaultBasePeriods
									: RateBaseTypeEnum.DAYLY_365.getBase(),
							amount, yearRate, interestRoundingMode, totalBaseCount); // 默认以365天为基准， 按日计算实际总利息
					interest = totalInterest.subtract(distributedInterest);
				}
			} else {
				capital = periodCapital;
			}
			if (BigDecimal.ZERO.equals(interest)) {
				interest = calculateInterest(basePeriods, amount, yearRate, interestRoundingMode, realPeriods);// 等额本金的金额基数是剩余未还本金
			}

			calculateAmount = calculateAmount.subtract(capital);

			validate(interest, capital, calculateAmount);

			planList.add(RepayPlan.init(loanDto.getLoanNo(), loanDto.getGraceDays(), curPeriod, periodEndDate, capital,
					interest, calculateAmount));

			periodBeginDate = periodEndDate; // 下一期的起息日
			curPeriod++;
			distributedInterest = distributedInterest.add(interest);

		}
		return planList;
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS005;
	}
}
