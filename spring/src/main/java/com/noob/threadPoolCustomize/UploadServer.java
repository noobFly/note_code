
package com.noob.threadPoolCustomize;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UploadServer {
	private Long id;
	/** 类型，ftp/ftps/sftp/oss/tango */
	private String type;
	/** 服务器编号 */
	private String serverCode;
	/** 服务器名称/描述 */
	private String serverName;
	/** 主机信息，可为ip或URL等 */
	private String host;
	/** 端口 */
	private Integer port;
	/** 用户名 */
	private String username;
	/** 最小连接数 */
	private Integer minActive;
	/** 最大连接数 */
	private Integer maxActive;
	/** 密码 */
	private String password;
	/** json格式的拓展内容 */
	private Map<String, Object> extJson;
	/** 是否启用，1：是，0：否 */
	private Integer enable;
	/** 创建日期 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER, timezone = "GMT+8")
	private java.util.Date createTime;
	/** 更新日期 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER, timezone = "GMT+8")
	private java.util.Date updateTime;
	/** 使用环境，all-任何环境 dev-开发 test-测试 prod-生产，多个环境使用逗号分隔，与spring.profiles.active匹配 */
	private String profile;

}
