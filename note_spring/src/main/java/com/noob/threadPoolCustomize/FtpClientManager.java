package com.noob.threadPoolCustomize;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

/**
 * ftpclent连接池
 * 
 *
 *         获取连接使用结束后，调用ObjectPool的retureObject归还连接，不要使用FTPClient的disconnect;
 */
@Component
public class FtpClientManager extends AbstractClientManager<FTPClient, FtpClientPooledObjectFactory> {
	@Override
	public String getServerType() {
		return UploadServerType.FTP;
	}

	@Override
	protected String getDefaultServerCode() {
		throw new RuntimeException("未配置默认的ftp服务器");
	}
}
