package com.noob.sign.handler;

import java.util.Map;

import com.noob.sign.domain.TemplateInput;

/**
 * 输入合同/协议模板参数（域对象.成员变量），输出 域对象.成员变量：参数值 的键值对
 *
 */
public interface TemplateParamsHandler {

	/**
	 * 模板可以使用以下域参数
	 */
	public static interface TemplateDomain {
		String limitUse = "limitUse";
		String applyLimit = "applyLimit";
		String templateInput = "templateInput";
		String customer = "customer";

	}

	// 注入域对象
	<T> void setDomian(T domain, String key);

	// 指定默认域对象
	<T> void setDefaultDomain(T domain);

	Map<String, Object> handle(TemplateInput data);

}
