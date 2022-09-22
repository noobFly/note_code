package com.noob.threadPoolCustomize;

import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;

public abstract class AbstractClientFactory<T> extends BasePooledObjectFactory<T> {
	protected String serverHost;
	
	protected int serverPort;
	
	protected String username;
	
	protected String password;
	
	protected Map<String, Object> extMap;
  // 不同的sftp、ftp、ftps 客户端的创建是需要相同的参数
	public AbstractClientFactory(String serverHost, int serverPort, String username, String password,
			Map<String, Object> extMap) {
		super();
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.password = password;
		this.username = username;
		this.extMap = extMap;
	}
}
