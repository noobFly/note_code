package com.noob.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class NoCreator {


    /**
     * 获取一个21位长度的订单号
     *
     * @param pre 前缀传入3位
     * @return
     * @Description:
     */
    public static String generateNumber(String pre, String format) {
        StringBuilder sbTradeNo = new StringBuilder();
        sbTradeNo.append(pre);
        sbTradeNo.append(date2String(new Date(), format)).append(generateNumber(2));
        return sbTradeNo.toString();
    }

    public static String date2String(Date date, String format) {
        if (null == date || format == null || format.trim().length() == 0) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setLenient(false); // 设置Calendar是否宽松解析字符串，如果为false，则严格解析；默认为true，宽松解析
        String time = df.format(date);
        return time;
    }


    /**
     * 生成指定长度的纯数字随机串
     */
    public static String generateNumber(int codeLength) {
        // 10个数字
        final int maxNum = 8;
        int i; // 生成的随机数
        int count = 0; // 生成的密码的长度
        char[] str = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < codeLength) {
            // 生成随机数，取绝对值，防止生成负数
            i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为36-1

            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }

        return pwd.toString();
    }

    /**
     * 18身份证号码的结构 公民身份码是特征组合码，由十七位数字本体码和一位校验码组成。
     * <p>
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位校验码。
     * <p>
     * 1、地址码
     * <p>
     * 表示编码对象常住户口所在县(市、旗、区)的行政区域划分代码，按GB/T2260的规定执行。
     * <p>
     * 2、出生日期码
     * <p>
     * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。
     * <p>
     * 3、顺序码
     * <p>
     * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配给女性。
     */
    public static String getRandomID() {
        String id = "420222199204179999";
        // 随机生成省、自治区、直辖市代码 1-2
        String provinces[] = {"11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37",
                "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71",
                "81", "82"};
        String province = randomOne(provinces);
        // 随机生成地级市、盟、自治州代码 3-4
        String city = randomCityCode(18);
        // 随机生成县、县级市、区代码 5-6
        String county = randomCityCode(28);
        // 随机生成出生年月 7-14
        String birth = randomBirth(20, 50);
        // 随机生成顺序号 15-17(随机性别)
        String no = new Random().nextInt(899) + 100 + "";
        // 随机生成校验码 18
        /*
         * String checks[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "X" };
         * String check = randomOne(checks);
         */
        id = province + city + county + birth + no;
        String check = String.valueOf(IDCardValidateUtil.createCheckCode(id));

        // 拼接身份证号码
        id += check;
        return id;
    }

    /**
     * 从String[] 数组中随机取出其中一个String字符串
     *
     * @param s
     * @return
     */
    public static String randomOne(String s[]) {
        return s[new Random().nextInt(s.length - 1)];
    }

    /**
     * 随机生成两位数的字符串（01-max）,不足两位的前面补0
     *
     * @param max
     * @return
     */
    public static String randomCityCode(int max) {
        int i = new Random().nextInt(max) + 1;
        return i > 9 ? i + "" : "0" + i;
    }

    /**
     * 随机生成minAge到maxAge年龄段的人的生日日期
     *
     * @param minAge
     * @param maxAge
     * @return
     */
    public static String randomBirth(int minAge, int maxAge) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());// 设置当前日期
        // 随机设置日期为前maxAge年到前minAge年的任意一天
        int randomDay = 365 * minAge + new Random().nextInt(365 * (maxAge - minAge));
        date.set(Calendar.DATE, date.get(Calendar.DATE) - randomDay);
        return dft.format(date.getTime());
    }

    // 18位身份证号码各位的含义:
    // 1-2位省、自治区、直辖市代码；
    // 3-4位地级市、盟、自治州代码；
    // 5-6位县、县级市、区代码；
    // 7-14位出生年月日，比如19670401代表1967年4月1日；
    // 15-17位为顺序号，其中17位（倒数第二位）男为单数，女为双数；
    // 18位为校验码，0-9和X。
    // 作为尾号的校验码，是由把前十七位数字带入统一的公式计算出来的，
    // 计算的结果是0-10，如果某人的尾号是0－9，都不会出现X，但如果尾号是10，那么就得用X来代替，
    // 因为如果用10做尾号，那么此人的身份证就变成了19位。X是罗马数字的10，用X来代替10
    public static void main(String[] args) {
        System.out.println(NoCreator.getRandomID());
    }
}