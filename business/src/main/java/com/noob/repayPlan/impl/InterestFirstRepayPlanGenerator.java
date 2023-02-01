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
 * 按月先息后本
 * 
 */
public class InterestFirstRepayPlanGenerator extends AbstractRepayPlanGenerator {

	@Override
	public List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultBasePeriods) {
		List<RepayPlan> planList = new ArrayList<>();
		int periodCount = periodEndDateMap.size(); // 总期数
		Date periodBeginDate = loanDto.getStartDate();
		BigDecimal amount = loanDto.getAmount();
		BigDecimal yearRate = loanDto.getYearRate();
		RoundingMode interestRoundingMode = loanDto.getInterestRoundingMode();

		BigDecimal distributedInterest = BigDecimal.ZERO;
		int curPeriod = 1;
		for (Entry<Date, Boolean> dateEntry : periodEndDateMap.entrySet()) {
			Date periodEndDate = dateEntry.getKey();
			Boolean isDayRate = dateEntry.getValue();

			int realPeriods = isDayRate
					? calculateInterestDays(loanDto.isCalculateInterestFromNow(), periodBeginDate, periodEndDate)
					: 1;// 首期或指定：则用日利息计算。
			BigDecimal basePeriods = isDayRate && !RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType())
					? RateBaseTypeEnum.DAYLY_365.getBase()
					: defaultBasePeriods; // 首月按日计息基数365，其他月份则以入参为准

			BigDecimal capital = BigDecimal.ZERO;
			BigDecimal interest = BigDecimal.ZERO;
			if (curPeriod == periodCount) { // 最后一期
				capital = amount;
				if (loanDto.isEndComplementInterest()) {
					int totalDays = calculateInterestDays(loanDto.isCalculateInterestFromNow(), loanDto.getStartDate(),
							loanDto.getEndDate());
					BigDecimal totalInterest = calculateInterest(
							RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType()) ? defaultBasePeriods
									: RateBaseTypeEnum.DAYLY_365.getBase(),
							amount, yearRate, interestRoundingMode, totalDays);
					interest = totalInterest.subtract(distributedInterest);// 按日利息计算实际总利息 减掉已分配利息。
				}
			}

			if (BigDecimal.ZERO.equals(interest)) {
				interest = calculateInterest(basePeriods, amount, yearRate, interestRoundingMode, realPeriods);// 等额本金这的金额基数是： 剩余未还本金
			}

			validate(capital, interest);

			planList.add(RepayPlan.init(loanDto.getLoanNo(), loanDto.getGraceDays(), curPeriod, periodEndDate, capital,
					interest, BigDecimal.ZERO.equals(capital) ? amount : BigDecimal.ZERO));

			periodBeginDate = periodEndDate; // 下一期的起息日
			curPeriod++;
			distributedInterest = distributedInterest.add(interest);
		}
		return planList;
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS004;
	}

}
