package com.noob.login;

import java.util.Map;

public interface LoginCredentialMatcher {

	/**
	 * 密码一致性匹配
	 * @param origPassword 原密码
	 * @param encryptedPassword 已加密密码
	 * @return
	 */
    boolean match(String origPassword, String encryptedPassword);
	
    /**
     * 带拓展参数的，密码一致性匹配
     * @param origPassword 原密码
     * @param encryptedPassword 已加密密码
     * @param more 拓展参数
     * @return
     */
	default boolean match(String origPassword, String encryptedPassword, Map<String, Object> more) {
		return match(origPassword, encryptedPassword);
	}
	
	/**
	 * 加密/散列
	 * @param origPassword
	 * @return
	 */
    String encrypt(String origPassword);
    
    /**
     * 编号，使用数字编号，记录在数据库中，避免加密方式信息泄露
     * @return
     */
    Integer getMatcherCode();
    
    /**
     * 名称
     * @return
     */
    String getMatcherName();
}
