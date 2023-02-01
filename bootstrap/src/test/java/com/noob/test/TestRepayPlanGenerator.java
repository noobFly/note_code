package com.noob.test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.noob.repayPlan.LoanParam;
import com.noob.repayPlan.RepayPlan;
import com.noob.repayPlan.RepayPlanGenerator.RateBaseTypeEnum;
import com.noob.repayPlan.impl.AverageCapitalAndInterestRepayPlanGenerator;
import com.noob.repayPlan.impl.AverageCapitalPlusInterestRepayPlanGenerator;
import com.noob.repayPlan.impl.AverageCapitalRepayPlanGenerator;
import com.noob.repayPlan.impl.InterestFirstRepayPlanGenerator;
import com.noob.repayPlan.impl.OneTimeRepayPlanGenerator;

public class TestRepayPlanGenerator {

	public static void oneTimeRepaymentPlanGeneratorTest() throws ParseException {
		LoanParam dto = buildParam();
		dto.setRateBaseType(RateBaseTypeEnum.DAYLY_365.getType());
		print(new OneTimeRepayPlanGenerator().handle(dto));

	}

	public static void interestFirstRepaymentPlanGeneratorTest() throws ParseException {
		LoanParam dto = buildParam();
		dto.setRateBaseType(RateBaseTypeEnum.MONTH.getType());
		print(new InterestFirstRepayPlanGenerator().handle(dto));

	}

	public static void averageCapitalPlusInterestRepayPlanGeneratorTest() throws ParseException {
		LoanParam dto = buildParam();
		dto.setRateBaseType(RateBaseTypeEnum.MONTH.getType());
		print(new AverageCapitalPlusInterestRepayPlanGenerator().handle(dto));

	}

	public static void averageCapitalAndInterestRepayPlanGeneratorTest() throws Exception {
		LoanParam dto = buildParam();
		dto.setRateBaseType(RateBaseTypeEnum.MONTH.getType());
		print(new AverageCapitalAndInterestRepayPlanGenerator().handle(dto));

	}

	public static void averageCapitalRepayPlanGeneratorTest() throws Exception {
		LoanParam dto = buildParam();
		dto.setRateBaseType(RateBaseTypeEnum.DAYLY_365.getType());
		print(new AverageCapitalRepayPlanGenerator().handle(dto));

	}

	private static LoanParam buildParam() throws ParseException {
		LoanParam dto = new LoanParam();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		dto.setStartDate(df.parse("2023-01-29"));
		dto.setEndDate(df.parse("2053-01-01"));
		dto.setTotalPeriod(12*30);
		dto.setAmount(new BigDecimal("3960000"));
		dto.setPeriodMinDay(10);
		dto.setRepaymentDay(1);
		dto.setYearRate(new BigDecimal("3.65"));
		dto.setLoanNo("testLoan123456");
		dto.setGraceDays(2);

		return dto;
	}

	private static void print(List<RepayPlan> handle) {
	//	System.out.println(JSON.toJSONStringWithDateFormat(handle, "yyyy-MM-dd"));
	}

	public static void main(String[] args) throws Exception {
		try {
			oneTimeRepaymentPlanGeneratorTest();
				interestFirstRepaymentPlanGeneratorTest();
			averageCapitalPlusInterestRepayPlanGeneratorTest();
				averageCapitalAndInterestRepayPlanGeneratorTest();
			averageCapitalRepayPlanGeneratorTest();

		} catch (Exception e) {
			throw e;
		}
	}
}
