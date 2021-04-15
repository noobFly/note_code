package com.noob.util;

import java.util.regex.Pattern;

/**
 * 正则规则校验工具类
 */
public final class RegExValidateUtils {
    private static final String EMAIL_PATTERN = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    private static final String TELEPHONE_PATTERN = "^(^0\\d{2}-?\\d{8}$)|(\\([0-9]+\\))?-?[0-9]{7,8}|(^0\\d{3}-?\\d{7}$)|(^0\\d2-?\\d{8}$)|(^0\\d3-?\\d{7}$)$";
    private static final String MOBILE_PATTERN = "^((0|((([+]*)?\\d*)?86|17951)(\\-| )?))?(1[1-9][0-9])[0-9]{8}$";
    private static final String IDCARD_PATTERN = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";
    private static final String IP_PATTERN = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])"
            + "((\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){3}|(\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){5})$";
    private static final String BANK_CARD_PATTERN = "^(\\d{4} ?){2,5}\\d{2,4}$";

    private static final Pattern IDCARD_REG = Pattern.compile(IDCARD_PATTERN);
    private static final Pattern MOBILE_REG = Pattern.compile(MOBILE_PATTERN);
    private static final Pattern TELEPHONE_REG = Pattern.compile(TELEPHONE_PATTERN);
    private static final Pattern PHONE_REG = Pattern.compile("(" + TELEPHONE_PATTERN + ")|(" + MOBILE_PATTERN + ")");
    private static final Pattern EMAIL_REG = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern IP_REG = Pattern.compile(IP_PATTERN);
    private static final Pattern BANK_CARD_REG = Pattern.compile(BANK_CARD_PATTERN);

    /**
     * 手机号验证
     *
     * @param mobile 手机号
     * @return
     */
    public static boolean isMobile(String mobile) {
        if (isEmpty(mobile)) {
            return false;
        }
        boolean isMobile = MOBILE_REG.matcher(mobile).matches();
        return isMobile;
    }

    /**
     * 是否是固定电话
     *
     * @param telephone 固定电话
     * @return
     */
    public static boolean isTelephone(String telephone) {
        if (isEmpty(telephone)) {
            return false;
        }
        boolean isTelephone = TELEPHONE_REG.matcher(telephone).matches();
        return isTelephone;
    }

    /**
     * 电话验证，包含固话和手机号
     *
     * @param phone 号码
     * @return
     */
    public static boolean isPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        boolean isPhone = PHONE_REG.matcher(phone).matches();
        return isPhone;
    }

    /**
     * 身份证验证，判断是否15位数字或者17位数字+1位校验码
     *
     * @param idCard idCard
     * @return
     */
    public static boolean isIDCard(String idCard) {
        if (isEmpty(idCard)) {
            return false;
        }
        boolean isIdCard = IDCARD_REG.matcher(idCard).matches();
        return isIdCard;
    }

    /**
     * 身份证严格验证，按照国家标准校验身份证，包括格式，第一级地区码，生日有效性，校验码有效性
     *
     * @param idCard idCard
     * @return
     */
    public static boolean isIDCardStrict(String idCard) {
        return IDCardValidateUtil.isIdCardValidate(idCard);
    }

    /**
     * 邮箱验证
     *
     * @param email email
     * @return
     */
    public static boolean isEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        boolean isEmail = EMAIL_REG.matcher(email).matches();
        return isEmail;
    }

    /**
     * 兼容ipv4和v6的校验
     *
     * @param ip ip地址
     * @return
     */
    public static boolean isIPAddress(String ip) {
        if (isEmpty(ip)) {
            return false;
        }
        boolean isIPAddress = IP_REG.matcher(ip).matches();
        return isIPAddress;
    }

    /**
     * 匹配10到24位数字的银行卡，不校验银行卡内容是否有效，只关心位数
     *
     * @param bankCard 银行卡号
     * @return
     */
    public static boolean isBankCard(String bankCard) {
        if (isEmpty(bankCard)) {
            return false;
        }
        boolean isBankCard = BANK_CARD_REG.matcher(bankCard).matches();
        return isBankCard;
    }

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

}