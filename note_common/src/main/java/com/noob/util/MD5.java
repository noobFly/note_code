package com.noob.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5 加密后的位数一般为两种，16 位与 32 位。16 位实际上是从 32 位字符串中，取中间的第 9 位到第 24 位的部分
 */
public class MD5 {
	/**
	 * 16进制字符
	 */
	static String[] chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	public static byte[] md5_16(String key) {
		MessageDigest md5;
		try {
			// 生成一个MD5加密计算摘要
			md5 = MessageDigest.getInstance("MD5");// 16位
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		md5.reset();
		byte[] bytes;
		try {
			bytes = key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		// 计算md5函数
		md5.update(bytes);
		// digest()最后确定返回md5 hash值，hash值是16位的hex值，实际上就是8位的字符
		byte[] digest = md5.digest();
		return digest;
	}

	public static String md5_32_1(String key) {
		byte[] byteArray = md5_16(key);
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

	public static String md5_32_2(String key) {
		StringBuilder sb = new StringBuilder(32);
		byte[] result = md5_16(key);
		// 将结果转为16进制字符 0~9 A~F
		for (int i = 0; i < result.length; i++) {
			// 一个字节对应两个字符
			byte x = result[i];
			// 取得高位
			int h = 0x0f & (x >>> 4);
			// 取得低位
			int l = 0x0f & x;
			sb.append(chars[h]).append(chars[l]);
		}
		return sb.toString();

	}

	// BigInteger函数将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
	// 一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
	public static String md5_32_3(String key) {
		String md5code = new BigInteger(1, md5_16(key)).toString(16);
		// 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
        	md5code = "0" + md5code;
        }
        return md5code;
	}

	// org.springframework.util.DigestUtils 也提供
	public static String md5_32_4(String key) {
		return DigestUtils.md5Hex(key);
	}

	public static void main(String[] args) {
		String key = "12321321";
		System.out.println(md5_32_1(key));
		System.out.println(md5_32_2(key));
		System.out.println(md5_32_3(key));
		System.out.println(md5_32_4(key));
		System.out.println(md5_32_4(key));
		System.out.println(md5_32_4(key).substring(8,24));

	}
}
