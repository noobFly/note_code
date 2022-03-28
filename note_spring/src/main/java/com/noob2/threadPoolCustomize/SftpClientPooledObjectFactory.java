package com.noob2.threadPoolCustomize;

import java.util.Map;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.jcraft.jsch.ChannelSftp;
import com.noob.util.File.SftpUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SftpClientPooledObjectFactory extends AbstractClientFactory<ChannelSftp> {
	public SftpClientPooledObjectFactory(String serverHost, int serverPort, String username, String password,
			Map<String, Object> extMap) {
		super(serverHost, serverPort, username, password, extMap);
	}

	@Override
	public ChannelSftp create() throws Exception {
		ChannelSftp channelSftp = SftpUtils.getSftpClient(serverHost, serverPort, username, password);
		return channelSftp;
	}

	
	@Override
	public PooledObject<ChannelSftp> wrap(ChannelSftp obj) {
		return new DefaultPooledObject<ChannelSftp>(obj);
	}

	@Override
	public void destroyObject(PooledObject<ChannelSftp> p) throws Exception {
		try {
			ChannelSftp channelSftp = p.getObject();
			channelSftp.disconnect();
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public boolean validateObject(PooledObject<ChannelSftp> p) {
		ChannelSftp channelSftp = p.getObject();
		try {
			if (channelSftp.isClosed()) {
				return false;
			}

			if (!channelSftp.isConnected()) {
				return false;
			}

			channelSftp.cd("/");
			return true;
		} catch (Exception e) {
			log.info("", e);
		}
		return false;
	}

}
