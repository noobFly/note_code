package com.noob.login;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

/**
 * BCrypt是单向Hash加密算法，一般用于密码加密，相对来说，BCrypt比MD5更安全 ，但是MD5加密会更快速。
 * MD5可被撞库破解
 * 而Bcrypt类似Pbkdf2算法不可反向破解生成明文（不可逆加密），BCrypt不支持反运算， 只支持密码校验。
 * 虽然不能转换成明文密码，但是它还是会被彩虹破解，只是相对于破解时间来说两者差别巨大，如果非要举例说明一下，用彩虹破解MD5可能需要3分钟左右，而BCrypt就需要14年之久。
 * 所以一般都推荐使用bcrypt。
 * BCrypt加密原理：
 * 输入的明文密码通过10次循环加盐后得到myHash（版本+salt），然后存入数据库。
 * 系统在验证用户的口令时，需要从myHash中取出salt跟password进行hash；得到的结果保存在DB中的hash进行比对，如果一致才算验证通过。
 */
public class BCryptCredentialMatcher implements LoginCredentialMatcher {
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static final Integer MATCHER_CODE = 1;

    @Override
    public boolean match(String origPassword, String encryptedPassword) {
        if (origPassword == null) {
            return false;
        }
        Assert.notNull(encryptedPassword, "已加密密码不能为空");
        return passwordEncoder.matches(origPassword, encryptedPassword);
    }

    @Override
    public String encrypt(String origPassword) {
        return passwordEncoder.encode(origPassword);
    }

    @Override
    public Integer getMatcherCode() {
        return MATCHER_CODE;
    }

    @Override
    public String getMatcherName() {
        return "BCrypt";
    }

    /**
     * $2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi
     * BCrypt有四个变量：
     *
     * "$2a$":           BCrypt的版本
     * "10":                              saltRounds: 正数，代表hash杂凑次数，数值越高越安全，默认10次。
     * myPassword: 明文密码字符串。
     * "EKXOFOjn2Bve3t45194KkO":                salt: 盐，一个128bits随机字符串，22字符
     * "dzGzywmeRw3yeekVf.YeURs2Z4.IaHi":     myHash: 经过明文密码password和盐salt进行hash，个人的理解是默认10次下 ，循环加盐hash10次，得到myHash
     *
     * 每次明文字符串myPassword过来，就通过10次循环加盐salt加密后得到myHash, 然后拼接BCrypt版本号+salt盐+myHash等到最终的bcrypt密码 ，存入数据库中。
     * 这样同一个密码，每次登录都可以根据自省业务需要生成不同的myHash, myHash中包含了版本和salt，存入数据库。
     * @param args
     */
    public static void main(String[] args) {
        BCryptCredentialMatcher matcher = new BCryptCredentialMatcher();
        String code1= matcher.encrypt("123"); // $2a$10$oAijvfXbkYtm9Qm8bbdWM.iOEqD./t.2cbez9qbMpMQOfSFwoo4eK
        String code2 = matcher.encrypt("123"); //  $2a$10$Td5PJeyvRUMgd5vH0XLaaOvO.qJzVgE0n6vvuP53XKDpoCzz1wJD.
        String code3 = new BCryptCredentialMatcher().encrypt("123");
        System.out.println(code1);
        System.out.println(code2);
        System.out.println(code3);
        System.out.println(matcher.match("123", code1));
        System.out.println(matcher.match("123", code2));
        System.out.println(matcher.match("123", code3));

    }


}
