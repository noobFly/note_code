package com.noob.workflow;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;

import java.util.Date;


@Data
public class SysUser {
	private static final long serialVersionUID = 1L;


	@JsonFormat(shape = Shape.STRING)
	private Long userId;

	/** 部门ID */
	@JsonFormat(shape = Shape.STRING)
	private Long deptId;

	/** 用户账号 */

	private String userName;

	/** 用户昵称 */

	private String nickName;
	
	/** 用户类型（00系统用户） */
	private String userType;


	/** 密码 */
	@JsonProperty(access = Access.WRITE_ONLY) //反序列化时不序列化
	private String password;
	
	@JsonIgnore
	private Integer passwordType; //程序内部使用，不接收，不输出
	
	/** 密码过期时间 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date passwordExpireTime;

	/** 帐号状态（0正常 1停用） */
	private String status;

	/** 删除标志（0代表存在 2代表删除） */
	private String delFlag;


	public boolean isAdmin() {
		return userId != null && 1L == userId;
	}
}
