package com.noob.gateway.openApi;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class AppSecurityDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	/** 平台公钥 */
	private String platformPublicKey;
	/** 平台私钥 */
	private String platformPrivateKey;
	/** 应用公钥 */
	private String appPublicKey;
	/**
	 * 白名单配置
	 */
	private List<String> ipWhitelistList;
	/**
	 * appId 绑定的渠道
	 */
	private List<String> bindChannelList;
	/** 开发者是否被禁用 */
	private boolean developerForbidden = false;
	/** 应用是否被禁止/失效 */
	private boolean applicationForbidden = false;

}
