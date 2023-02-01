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
 * 由此可得第k个月后所欠银行贷款为 A(1+β)^k –X[1+(1+β)+(1+β)^2+…+(1+β)^(k-1)]= A(1+β)^k
 * –X[(1+β)^k - 1]/β = 0
 * <p>
 * 推得： 每期还款本息总额 x = A * β * (1 + β) ^ k / [(1 + β) ^ k - 1]
 * <p>
 * 先计算出每月应还总金额（不变）, 再用剩余总本金*月利率得到每月应还利息。 差额就是每月还款本金
 * 
 */
public class AverageCapitalPlusInterestRepayPlanGenerator extends AbstractRepayPlanGenerator {
	/**
	 * 按日计息时：
	 * <p>
	 * 最后一期仍然用每期应还总额-剩余本金 = 利息 的方式得出的利息可能为负数。
	 * <p>
	 * 因为每期应还总额是按月利率算出的值，每期还的总额固定。 按日计息则可能当前期利息收的会多，本金收的少。那下一期剩余本金就多，再计算利息也多，这样....每一期的剩余本金比正常情况下会偏多。
	 * <p>
	 * 最终：最后一剩余本金可能会超出每期应还总额的值 , 这种情况最后一期利息就按日计算！！
	 */
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
			int realPeriods = isDayRate
					? calculateInterestDays(loanDto.isCalculateInterestFromNow(), periodBeginDate, periodEndDate)
					: 1;// 首期或指定 则用日利息计算。

			BigDecimal basePeriods = isDayRate && !RateBaseTypeEnum.useDayRate(loanDto.getRateBaseType())
					? RateBaseTypeEnum.DAYLY_365.getBase()
					: defaultBasePeriods;
			if (curPeriod == periodCount) {
				capital = calculateAmount;
				if (isDayRate) { // 末期 & 按日计息 （分期还款只有第一期可能是会换成按日计息）
					interest = calculateInterest(basePeriods, calculateAmount, yearRate,
							loanDto.getInterestRoundingMode(), realPeriods);
				} else {
					interest = periodRepayAmount.subtract(capital); // 末期正常情况： 利息 = 应还总金额 - 本金
				}
			} else {

				interest = calculateInterest(basePeriods, calculateAmount, yearRate, loanDto.getInterestRoundingMode(),
						realPeriods);
				capital = periodRepayAmount.subtract(interest); // 非末期： 先计算剩余未还金额的利息，剩下的就是当期应还本金
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

	private static BigDecimal getPeriodRepayAmount(BigDecimal amount, BigDecimal monthRate, int totalPeriods,
			RoundingMode roundingMode) {
		double aprPow = Math.pow(1 + monthRate.doubleValue(), totalPeriods);
		double denominator = 1;
		if (aprPow > 1) {
			denominator = aprPow - 1;
		}
		return amount.multiply(monthRate).multiply(BigDecimal.valueOf(aprPow)).divide(BigDecimal.valueOf(denominator),
				2, roundingMode);
	}

	/**
	 * 标准利息 *
	 * <p>
	 * t-还款月序号
	 * <p>
	 * A * β * ( (1+β)^K - (1+β)^(t-1) ) / ( (1+β)^K - 1)
	 * <p>
	 * 首月利息 = A * β * ( (1+β)^K - (1+β)^(1-1) ) / ( (1+β)^K - 1) = A * β * ( (1+β)^K - 1) / ( (1+β)^K - 1) = A * β
	 * 
	 * @return
	 */
	private static BigDecimal standardInterest(int curPeriod, BigDecimal amount, BigDecimal monthRateRate,
			int totalPeriods, RoundingMode roundingMode) {
		BigDecimal base = monthRateRate.add(BigDecimal.ONE);
		BigDecimal total = new BigDecimal(Math.pow(base.doubleValue(), totalPeriods));
		return amount.multiply(monthRateRate)
				.multiply(total.subtract(new BigDecimal(Math.pow(base.doubleValue(), curPeriod - 1))))
				.divide(total.subtract(BigDecimal.ONE), 2, roundingMode);
	}

	@Override
	public String getRepayMode() {
		return RepayMode.SYS002;
	}

	public static void main(String[] args) {
		System.out.println(standardInterest(1, new BigDecimal("5000"),
				new BigDecimal("36").divide(BigDecimal.valueOf(12 * 100), 48, RoundingMode.DOWN), 6,
				RoundingMode.HALF_UP));
		System.out.println(getPeriodRepayAmount(new BigDecimal("5000"),
				new BigDecimal("36").divide(BigDecimal.valueOf(12 * 100), 48, RoundingMode.DOWN), 6,
				RoundingMode.HALF_UP));
		System.out.println(new BigDecimal("5000")
				.multiply(new BigDecimal("36").divide(BigDecimal.valueOf(12 * 100), 48, RoundingMode.DOWN)));
	}

}
