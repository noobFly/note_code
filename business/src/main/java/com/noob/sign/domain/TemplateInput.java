package com.noob.sign.domain;

import lombok.Data;

/**
 * 合同/协议参数构建输入
 * <p>
 * 存储用户获取域对象的查询条件或需要传递的值
 *
 */
@Data
public class TemplateInput {
	/**
	 * 申请流水号，即授信或支用的流水号
	 */
	private String flowNo;
	/**
	 * 需要提取的参数名
	 * <p>
	 * eg.
	 * templateInput.flowNo,templateInput.channel,applyLimit.loanType,customer.customerName
	 */
	private String templateParams;

	private String channel;

	private String loanType;
}
