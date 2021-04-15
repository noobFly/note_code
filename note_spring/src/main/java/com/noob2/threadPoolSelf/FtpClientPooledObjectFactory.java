package com.noob2.threadPoolSelf;

import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.noob.util.FtpUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpClientPooledObjectFactory extends AbstractClientFactory<FTPClient> {

	public FtpClientPooledObjectFactory(String serverHost, int serverPort, String username, String password,
			Map<String, Object> extMap) {
		super(serverHost, serverPort, username, password, extMap);
	}

	@Override
	public FTPClient create() throws Exception {
		FTPClient ftpsClient = FtpUtils.getFTPClient(serverHost, serverPort, username, password);
		return ftpsClient;
	}

	@Override
	public PooledObject<FTPClient> wrap(FTPClient obj) {
		return new DefaultPooledObject<FTPClient>(obj);
	}

	@Override
	public void destroyObject(PooledObject<FTPClient> p) throws Exception {
		try {
			FTPClient ftpsClient = p.getObject();
			ftpsClient.logout();
			ftpsClient.disconnect();
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public boolean validateObject(PooledObject<FTPClient> p) {
		FTPClient ftpClient = p.getObject();
		try {
			if (!ftpClient.isConnected()) {
				return false;
			}
			if (!ftpClient.isAvailable()) {
				return false;
			}
			ftpClient.changeWorkingDirectory("/");
			return true;
		} catch (Exception e) {
			log.info("", e);
		}
		return false;
	}

}
