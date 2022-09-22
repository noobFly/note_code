
package com.noob.sign.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Customer {
	private Long id;
	/** 客户名称 */
	private String customerName;
	/** 证件号码 */
	private String certificateNo;

}
