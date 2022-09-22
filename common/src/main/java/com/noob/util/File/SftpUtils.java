package com.noob.util.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SftpUtils {

	/**
	 * 
	 * @param sftp
	 * @param remotePath 远程保存文件路径，含文件名
	 * @param plainText  文本数据
	 * @throws IOException
	 */
	public static void uploadPlainText(ChannelSftp sftp, String remotePath, String plainText) throws IOException {
		try {
			File file = new File(remotePath);
			createDir(sftp, transformLinuxFileSeparator(remotePath));
			sftp.put(new ByteArrayInputStream(plainText.getBytes("UTF-8")), file.getName());
			log.info("上传文件到{}成功", remotePath);
		} catch (Exception e) {
			log.error("上传文件到ftp出错", e);
			throw new RuntimeException("上传文件到ftp出错:" + remotePath);
		}
	}

	/**
	 * 
	 * @param sftp
	 * @param remotePath    远程保存文件路径，含文件名
	 * @param localFilePath 要上传的本地文件路径，含文件名
	 * @throws IOException
	 */
	public static void uploadFile(ChannelSftp sftp, String remotePath, String localFilePath) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(localFilePath);
			createDir(sftp, transformLinuxFileSeparator(remotePath));
			sftp.put(fis, new File(remotePath).getName());
			log.info("上传文件到{}成功", remotePath);
		} catch (Exception e) {
			log.error("上传文件到ftp出错", e);
			throw new RuntimeException("上传文件到ftp出错:" + remotePath);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ChannelSftp getSftpClient(String host, Integer port, String user, String password) {
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			Session sshSession = jsch.getSession(user, host, port);
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			log.info("SFTP Session connected.");
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
		} catch (Exception e) {
			log.error("连接sftp失败！host {} ,port {}, user:{} password:{}", host, port, user, password, e);
			throw new RuntimeException("连接SFTP失败:" + e.getMessage());
		}
		return sftp;
	}

	/**
	 * 连接sftp服务器
	 *
	 * @param host     主机
	 * @param port     端口
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 */
	public static ChannelSftp connect(String host, String port, String username, String password) {
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			jsch.getSession(username, host, Integer.valueOf(port));
			Session sshSession = jsch.getSession(username, host, Integer.valueOf(port));
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			log.info("SFTP Session connected.");
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			log.info("Connected to " + host);
		} catch (Exception e) {
			log.error("连接sftp失败: host {} ,user {} ", host, username, e);
			throw new RuntimeException("连接SFTP失败:" + e.getMessage());
		}
		return sftp;
	}

	/**
	 * 断开连接
	 */
	public static void disconnect(ChannelSftp sftp) {
		if (sftp == null) {
			return;
		}
		try {
			sftp.getSession().disconnect();
		} catch (JSchException e) {
			log.error("关闭sftp连接出错", e);
		}
		sftp.quit();
		sftp.disconnect();
	}

	/**
	 * 创建目录(有则切换目录，没有则创建目录)
	 *
	 * @param directory
	 * @return
	 * @throws SftpException
	 */
	public static boolean createDir(ChannelSftp sftp, String directory) {
		try {
			sftp.cd(directory);
		} catch (SftpException e) {
			try {
				// 目录不存在，则创建文件夹
				String[] dirs = directory.split("/");
				StringBuffer sbfDir = new StringBuffer();
				for (String dir : dirs) {
					if (null == dir || "".equals(dir)) {
						continue;
					}
					sbfDir.append("/");
					sbfDir.append(dir);
					try {
						sftp.cd(sbfDir.toString());
					} catch (SftpException ex) {
						sftp.mkdir(sbfDir.toString());
						sftp.cd(sbfDir.toString());
					}
				}
			} catch (Exception e1) {
				log.error("在sftp上面创建目录失败", e1);
				throw new RuntimeException("在sftp上面创建目录失败" + e1.getMessage());
			}
		}
		return true;
	}

	/**
	 * 下载并转为流数据
	 * 
	 * @param sftp
	 * @param remotePath
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayInputStream downloadFile(ChannelSftp sftp, String remotePath) {
		try {
			File file = new File(remotePath);
			createDir(sftp, transformLinuxFileSeparator(remotePath));
			InputStream inputStream = sftp.get(file.getName());
			log.info("从sftp下载文件{}成功", remotePath);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, bos);

			return new ByteArrayInputStream(bos.toByteArray());
		} catch (Exception e) {
			log.error("从sftp下载文件出错", e);
			if (e instanceof SftpException) {
				e = new SftpException(0, e.getMessage() + ": " + remotePath, e);
			}
			throw new RuntimeException("从sftp下载文件出错:" + remotePath);
		}
	}

	/**
	 * 下载到目录
	 * 
	 * @param sftp
	 * @param remotePath
	 * @param localDir
	 * @throws IOException
	 */
	public static void downloadFile(ChannelSftp sftp, String remotePath, String localDir) {
		try {
			File file = new File(remotePath);
			createDir(sftp, transformLinuxFileSeparator(remotePath));
			sftp.get(file.getName(), localDir);
			log.info("从sftp下载文件{}成功", remotePath);
		} catch (Exception e) {
			log.error("从sftp下载文件出错", e);
			throw new RuntimeException("从sftp下载文件出错:" + remotePath);
		}
	}

	/*
	 * 远程路径为linux，本地为Windows时，路径 分隔符需要替换一下
	 */
	private static String transformLinuxFileSeparator(String remoteFile) {
		String remoteDir = new File(remoteFile).getParent();
		return File.separator.equals("\\") && remoteDir.indexOf("\\") > -1
				? remoteDir.replace("\\", FtpUtils.LINUX_SEPARATOR)
				: remoteDir;
	}

}
