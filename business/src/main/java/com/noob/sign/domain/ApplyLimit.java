
package com.noob.sign.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApplyLimit {

	/** 申请流水号 */
	private String flowNo;
	/** 渠道编码 */
	private String channel;
	/** 客户ID */
	private Long customerId;
	/** 证件号码 */
	private String certificateNo;
	/** 客户名称 */
	private String customerName;
	/** 产品类型 */
	private String loanType;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
