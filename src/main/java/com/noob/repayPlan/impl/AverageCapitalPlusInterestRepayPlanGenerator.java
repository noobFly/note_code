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
 * 等额本息 （每期还款金额相同）
 * <p>
 * 等比数列求和: S = a1 * (1 - q ^ n ) / (1- q)
 * <p>
 * 假设贷款总金额为A，月利率为β，贷款期数为k，每期需还款总金额（本金+利息）为x，则各个月所欠银行贷款为：
 * <p>
 * 第一个月A(1+β)-X
 * <p>
 * 第二个月(A(1+β)-X)(1+β)-X=A(1+β)^2-X[1+(1+β)]
 * <p>
 * 第三个月[A(1+β)-X)(1+β)-X](1+β)-X =A(1+β)^3-X[1+(1+β)+(1+β)^2]
 * <p>
 * …
 * <p>
 * 由此可得第k个月后所欠银行贷款为 A(1+β)^k –X[1+(1+β)+(1+β)^2+…+(1+β)^(k-1)]= A(1+β)^k –X[(1+β)^k - 1]/β = 0
 * <p>
 * 推得： 每期还款本息总额 x = A * β * (1 + β) ^ k / [(1 + β) ^ k - 1]
 * <p>
 * 先计算出每月应还总金额（不变）, 再用剩余总本金*月利率得到每月应还利息。 差额就是每月还款本金
 * 
 */
public class AverageCapitalPlusInterestRepayPlanGenerator extends AbstractRepayPlanGenerator {

	@Override
	public List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultBasePeriods) {
		List<RepayPlan> planList = new ArrayList<>();
		int periodCount = periodEndDateMap.size(); // 总期数
		Date periodBeginDate = loanDto.getStartDate();
		BigDecimal amount = loanDto.getAmount();
		BigDecimal yearRate = loanDto.getYearRate();

		BigDecimal periodRate = yearRate.divide(BigDecimal.valueOf(12 * 100), CALCULATE_SCALE, RoundingMode.DOWN);
		BigDecimal periodRepayAmount = getPeriodRepayAmount(loanDto.getAmount(), periodRate, periodCount,
				loanDto.getPeriodAmountRoundingMode()); // 计算每个周期的还款金额
		BigDecimal calculateAmount = amount; // 剩余计息本金
		int curPeriod = 1;
		for (Entry<Date, Boolean> dateEntry : periodEndDateMap.entrySet()) {
			Date periodEndDate = dateEntry.getKey();
			Boolean isDayRate = dateEntry.getValue();

			BigDecimal interest = BigDecimal.ZERO;
			BigDecimal capital = BigDecimal.ZERO;

			if (curPeriod == periodCount) {
				capital = calculateAmount;
				interest = periodRepayAmount.subtract(capital);
			} else {
				int realPeriods = isDayRate
						? calculateInterestDays(loanDto.isCalculateInterestFromNow(), periodBeginDate, periodEndDate)
						: 1;// 首期或指定 则用日利息计算。

				BigDecimal basePeriods = isDayRate && !RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType())
						? RateBaseTypeEnum.DAYLY_365.getBase()
						: defaultBasePeriods;

				interest = calculateInterest(basePeriods, calculateAmount, yearRate, loanDto.getInterestRoundingMode(),
						realPeriods);
				capital = periodRepayAmount.subtract(interest); // 先计算剩余未还金额的利息，剩下的就是当期应还本金
			}

			calculateAmount = calculateAmount.subtract(capital); // 剩余未还本金

			validate(interest, capital, calculateAmount);

			planList.add(RepayPlan.init(loanDto.getLoanNo(), loanDto.getGraceDays(), curPeriod, periodEndDate, capital,
					interest, calculateAmount));

			periodBeginDate = periodEndDate; // 下一期的起息日
			curPeriod++;
		}
		return planList;
	}

	private static BigDecimal getPeriodRepayAmount(BigDecimal amount, BigDecimal monthRate, int periods,
			RoundingMode roundingMode) {
		double aprPow = Math.pow(1 + monthRate.doubleValue(), periods);
		double denominator = 1;
		if (aprPow > 1) {
			denominator = aprPow - 1;
		}
		return amount.multiply(monthRate).multiply(BigDecimal.valueOf(aprPow)).divide(BigDecimal.valueOf(denominator),
				2, roundingMode);
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS002;
	}

	public static void main(String[] args) {
		System.out.println(getPeriodRepayAmount(new BigDecimal("5000"),
				new BigDecimal("36").divide(BigDecimal.valueOf(12 * 100), 48, RoundingMode.DOWN), 6,
				RoundingMode.HALF_UP));
		System.out.println(new BigDecimal("5000")
				.multiply(new BigDecimal("36").divide(BigDecimal.valueOf(12 * 100), 48, RoundingMode.DOWN)));
	}

}
