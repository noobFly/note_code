package com.noob.repayPlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 还款计划生成器
 * <p>
 * 1、一次性还本付息、先息后本、等本等费的总利息相同
 * <p>
 * 2、等额本金与等本等费的差别是前者按剩余未还本金计算月利息，后者按总借款金额
 * <p>
 * 3、等额本金每月本金相同，不同的是利息
 * <p>
 * 4、等本等息 相当于 一次性还本付息均分到每一期。 每月的本金、利息基本相同
 * <p>
 * 5、等额本息每月还款金额相同，按剩余未还本金计算月利息， 差额就是每月还款本金
 * <p>
 * 6、指定还款日与首期最小天数合用。即使选择了按月计息，但指定了还款日, 首期依然按日计息(默认全年按365，后续可扩展)，余下的期数按指定计息方式
 * 
 * 
 * 
 */
@Slf4j
public abstract class AbstractRepayPlanGenerator implements RepayPlanGenerator {

	/**
	 * 运算时精度
	 */
	protected int CALCULATE_SCALE = 48;

	@Override
	public List<RepayPlan> handle(LoanParam loanDto) {
		check(loanDto);

		List<RepayPlan> list = calculate(loanDto);

		if (!RepayMode.SYS001.equals(getRepayMode())) {
			loanDto.setEndDate(
					list.stream().max(Comparator.comparing(RepayPlan::getPeriod)).get().getShouldRepaymentDate());
		} else {
			loanDto.setTotalPeriod(1);
		}
		if (log.isDebugEnabled()) {
			log.debug("{} 总利息：{}", this.getClass(),
					list.stream().map(t -> t.getShouldRepaymentInterest()).reduce(BigDecimal.ZERO, (a, b) -> a.add(b)));
		}

		return list;
	}

	private void check(LoanParam loanDto) {
		loanDto.checkParams();
		if (RepayMode.SYS001.equals(getRepayMode())) {
			if (loanDto.getEndDate() == null) {
				throw new IllegalArgumentException("一次性还本付息截息日不能为空");
			}
		} else if (loanDto.getTotalPeriod() <= 0) {
			throw new IllegalArgumentException("周期性还款方式总期数不能为空");
		}
	}

	protected List<RepayPlan> calculate(LoanParam loanDto) {
		Map<Date, Boolean> periodEndDateMap = periodEndDateMap(loanDto.getStartDate(), loanDto.getTotalPeriod(),
				loanDto.getAprBaseType(), loanDto.getRepaymentDay(), loanDto.getPeriodMinDay()); // 各期还款截止时间
		List<RepayPlan> list = calculate(loanDto, periodEndDateMap,
				RateBaseTypeEnum.get(loanDto.getAprBaseType()).getBase());

		return list;
	}

	/**
	 * 计算利息
	 * 
	 * @param basePeriods          基准周期
	 * @param amount               金额
	 * @param rate                 利率
	 * @param interestRoundingMode
	 * @param periods              周期
	 * @return
	 */
	protected BigDecimal calculateInterest(BigDecimal basePeriods, BigDecimal amount, BigDecimal rate,
			RoundingMode interestRoundingMode, int periods) {
		return amount.multiply(rate).multiply(BigDecimal.valueOf(periods)).divide(basePeriods, 2, interestRoundingMode);
	}

	protected Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	protected int calculateInterestDays(boolean isRaisingInterestFromN, Date beginDate, Date endDate) {
		int baseCount;
		baseCount = TimeUtil.getBetweenDays(endDate, beginDate); // 计息天数
		if (isRaisingInterestFromN) {
			baseCount += 1;
		}
		return baseCount;
	}

	/**
	 * 默认按月利率计息
	 * 
	 * @param startDate    开始时间
	 * @param totalPeriod  总期数
	 * @param repaymentDay 指定还款日
	 * @param aprBaseType  计息基数
	 * @param periodMinDay 周期最小天数 与指定还款日合用 针对第一期 范围在 [15, 25]
	 * @return 各期及是否需要按日计息
	 */
	protected Map<Date, Boolean> periodEndDateMap(Date startDate, int totalPeriod, int aprBaseType, int repaymentDay,
			int periodMinDay) {
		Map<Date, Boolean> dateMap = new LinkedHashMap<Date, Boolean>();
		boolean useDayRate = RateBaseTypeEnum.useDayRate(aprBaseType);

		Calendar periodCalendar = dateToCalendar(startDate);
		int beginDay = periodCalendar.get(Calendar.DAY_OF_MONTH);

		if (repaymentDay > 0) { // 指定了还款日
			boolean isHead = true;
			while (totalPeriod > 0) {
				periodCalendar.set(Calendar.DAY_OF_MONTH, repaymentDay); // 指定还款日,最好在28之前，不会出现还款日各月不一致的误差

				if (isHead) {
					if (repaymentDay <= beginDay) { // 还款日不大于开始日期 增加一个月
						periodCalendar.add(Calendar.MONTH, 1);
					}
					if (Duration.between(startDate.toInstant(), periodCalendar.toInstant()).toDays() < periodMinDay) {
						periodCalendar.add(Calendar.MONTH, 1); // 不满最小天数 再增加一个月
					}
					isHead = false;
				} else {
					periodCalendar.add(Calendar.MONTH, 1);
				}

				dateMap.put(periodCalendar.getTime(), isHead || useDayRate);// 首期需要按日利息计算
				totalPeriod--;
			}
		} else {
			int addMonth = 1;
			while (addMonth <= totalPeriod) {
				periodCalendar.setTime(startDate); // 防止月天数28、 29 、30 、 31 带来的误差
				periodCalendar.add(Calendar.MONTH, addMonth);
				addMonth++;
				dateMap.put(periodCalendar.getTime(), useDayRate);
			}
		}
		return dateMap;
	}

	public abstract List<RepayPlan> calculate(LoanParam loanDto, Map<Date, Boolean> periodEndDateMap,
			BigDecimal defaultDivBase);

}