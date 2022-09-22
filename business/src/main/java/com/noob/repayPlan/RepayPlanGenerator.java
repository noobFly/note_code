package com.noob.repayPlan;

import java.math.BigDecimal;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface RepayPlanGenerator {
	public List<RepayPlan> handle(LoanParam data);

	/**
	 * 还款方式标识
	 * 
	 * @return
	 */
	public String getRepayMode();

	/**
	 * 还款方式
	 * 
	 *
	 */
	public interface RepayMode {
		/**
		 * 一次性还本付息
		 */
		String SYS001 = "SYS001";
		/**
		 * 等额本息
		 */
		String SYS002 = "SYS002";
		/**
		 * 等额本金
		 */
		String SYS003 = "SYS003";
		/**
		 * 先息后本
		 */
		String SYS004 = "SYS004";
		/**
		 * 等本等息
		 */
		String SYS005 = "SYS005";
	}

	 // 都乘以了100, 使得年化利率传入的是实际年化*100
	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	enum RateBaseTypeEnum {
		/** 默认是月 */
		MONTH(0, BigDecimal.valueOf(12 * 100)),
		/** 按日，每年算360天 */
		DAYLY_360(1, BigDecimal.valueOf(360 * 100)),
		/** 按日，每年算365天 */
		DAYLY_365(2, BigDecimal.valueOf(365 * 100));

		private int type;
		private BigDecimal base;

		public static boolean useDayRate(int type) {
			return DAYLY_360.getType() == type || DAYLY_365.getType() == type;
		}

		public static RateBaseTypeEnum get(int type) {
			RateBaseTypeEnum result = RateBaseTypeEnum.MONTH;
			for (RateBaseTypeEnum element : RateBaseTypeEnum.values()) {
				if (element.getType() == type) {
					result = element;
					break;
				}
			}
			return result;
		}

	}

}
