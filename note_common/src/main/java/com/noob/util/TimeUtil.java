package com.noob.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public interface TimeUtil {
	/**
	 * 日期格式化
	 * 
	 * @param date   格式化的日期
	 * @param format 格式
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		try {
			DateFormat df = new SimpleDateFormat(format);
			return df.format(date);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	

	public static int getBetweenDays(Date fDate, Date sDate) {
		return (int) ((fDate.getTime() - sDate.getTime()) / 86400000L);// (24小时 * 60分 * 60秒 * 1000毫秒= 1天毫秒数)
	}

	/**
	 * 取日期当天的开始时间，即0时0分0秒
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateBegin(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 取日期当天的结束时间，即23时59分59秒
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateEnd(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
