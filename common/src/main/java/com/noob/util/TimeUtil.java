package com.noob.util;

import com.google.common.base.Strings;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil extends org.apache.commons.lang3.time.DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private static String[] parsePatterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    public static final String dateTime(final Date date) {
        return date == null ? null : formatDate(date, DATE_PATTERN);
    }

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
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
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
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }


    /**
     * 两个时间跨越的月数  （算头又算尾）
     *
     * @param endDate   截止时间
     * @param beginDate 开始时间
     * @return
     */
    public static int getMonthDiff(Date endDate, Date beginDate) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(endDate);
        c2.setTime(beginDate);
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        // 获取年的差值
        int yearInterval = year1 - year2;
        // 获取月数差值
        int monthInterval = month1 - month2;
        int monthsDiff = Math.abs(yearInterval * 12 + monthInterval) + 1;
        return monthsDiff;
    }

    /**
     * 获取某年第一天日期
     *
     * @return Date
     */
    public static Date getNowYearFirst() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取当年最后一天
     */
    public static Date getNowYearLast() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);// 负数标识从后向前

        return calendar.getTime();
    }

    /**
     * 获取当年开始的一天
     */
    public static Date getMonthFirst(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        return calendar.getTime();
    }

    /**
     * 获取当月最后一天
     */
    public static Date getMonthLast(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.clear(); // 清除
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.roll(Calendar.DAY_OF_MONTH, -1); // 负数标识从后向前
        return calendar.getTime();
    }

    /**
     * 获取当月月份  从 0开始!
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        return month;
    }

    /**
     * 计算某日期所在季度开始日期
     * 季度划分：1、2、3， 4、5、6， 7、8、9， 10、11、12
     */
    public static Date getSeasonEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, (month + 3) / 3 * 3);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Date(calendar.getTime().getTime() - 24 * 60 * 60 * 1000);
    }

    /**
     * 计算某日期所在季度结束日期
     * 季度划分：1、2、3， 4、5、6， 7、8、9， 10、11、12
     */
    public static Date getSeasonStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month / 3 * 3);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 所在季度
    static int getQuarterOfYear(String month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Strings.isNullOrEmpty(month) ? new Date() : parseDate(month));
        return calendar.get(Calendar.MONTH) / 3 + 1;
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }


    public static void main(String args[]) {
        Date now = new Date();
        System.out.println(formatDate(getSeasonStartDate(getNowYearFirst()), "yyyy-MM-dd HH:mm:ss:SSS"));
        System.out.println(formatDate(getSeasonEndDate(getNowYearFirst()), "yyyy-MM-dd HH:mm:ss:SSS"));
        System.out.println(getQuarterOfYear("2022-04-02"));
        System.out.println(formatDate(getServerStartDate(), "yyyy-MM-dd HH:mm:ss"));
        System.out.println(getDatePoor(getNowYearFirst(), getDateEnd(getNowYearLast())));
        System.out.println(formatDate(getDateBegin(now), "yyyy-MM-dd :: HH:mm:ss"));
        System.out.println(formatDate(getDateEnd(now), "yyyy-MM-dd :: HH:mm:ss"));
        System.out.println(getMonthDiff(now, now));
        System.out.println(formatDate(getNowYearFirst(), "yyyy-MM-dd"));
        System.out.println(formatDate(getNowYearLast(), "yyyy-MM-dd"));
        System.out.println(formatDate(getMonthFirst(now), "yyyy-MM-dd"));
        System.out.println(formatDate(getMonthLast(now), "yyyy-MM-dd"));
        System.out.println(getMonth(now));

    }


}
