package com.noob.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class FtpUtils {

	private static Logger logger = LoggerFactory.getLogger(FtpUtils.class);

	public static final String LINUX_SEPARATOR = "/";
	public static final String UTF8 = "UTF-8";

	/**
	 *
	 * @return
	 */
	public static FTPClient getFTPClient(String ip, Integer port, String user, String password) {
		try {
			FTPClient client = new FTPClient();
			client.connect(ip, port);
			client.enterLocalPassiveMode();
			client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			client.setControlEncoding(UTF8);
			client.setAutodetectUTF8(true);
			clientLogin(user, password, client);
			return client;
		} catch (Exception e) {
			logger.error("getFTPClient error! ip:{}, port:{}, user:{} password:{}", ip, port, user, password, e);
			throw new RuntimeException();
		}
	}

	/**
	 * TrustManager 默认是需要校验certificates ->
	 * TrustManagerUtils.getValidateServerCertificateTrustManager() acceptAll ==
	 * true 则不需要校验
	 *
	 * @return
	 */
	public static FTPSClient getFTPSClient(String ip, Integer port, String user, String password, boolean acceptAll) {
		try {
			FTPSClient client = new FTPSClient();
			if (acceptAll) {
				client.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
			}
			client.setAuthValue("SSL");
			client.connect(ip, port);
			client.enterLocalPassiveMode();
			client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			client.execPROT("P");
			client.execPBSZ(0);
			client.setControlEncoding(UTF8);
			client.setAutodetectUTF8(true);
			clientLogin(user, password, client);
			return client;
		} catch (Exception e) {
			logger.error("getFTPSClient error! ip:{}, port:{}, user:{} password:{}", ip, port, user, password, e);
			throw new RuntimeException();
		}
	}

	private static void clientLogin(String user, String password, FTPClient client) throws IOException {
		boolean loginFlag = client.login(user, password);
		if (!loginFlag) {
			throw new RuntimeException("获取FTPClient失败：登录失败，用户" + user);
		}
		// 二进制传输，非常重要，不然文件打不开的
		client.setFileType(FTP.BINARY_FILE_TYPE);
		if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) { // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码.
			client.setControlEncoding(UTF8);
		}
	}

	public static void close(FTPClient client) {
		if (client == null) {
			return;
		}
		try {
			client.logout();
			client.disconnect();
		} catch (Exception e) {
			logger.error("关闭FTPClient", e);
		}
	}

	/**
	 * 创建目录(有则切换目录，没有则创建目录)
	 *
	 * @param dir
	 * @return
	 */
	public static boolean createDir(FTPClient ftp, String dir) {
		if (StringUtils.isEmpty(dir))
			return true;
		String d;
		try {
			// 目录编码，解决中文路径问题
			d = encodePath(dir);
			// 尝试切入目录
			if (ftp.changeWorkingDirectory(d))
				return true;

			String[] arr = dir.split(LINUX_SEPARATOR);
			StringBuffer sbfDir = new StringBuffer();
			// 循环生成子目录
			for (String s : arr) {
				if (StringUtils.isBlank(s)) {
					continue;
				}
				sbfDir.append(LINUX_SEPARATOR).append(s);
				// 目录编码，解决中文路径问题
				String curDir = encodePath(sbfDir.toString()); // 兼容中文时，创建目录需要以GBK转iso-8859-1，切换目录以UTF-8转iso-8859-1，未明真相
				// 尝试切入目录
				if (ftp.changeWorkingDirectory(curDir))
					continue;
				if (!ftp.makeDirectory(curDir)) {
					logger.info("[失败]ftp创建目录：" + sbfDir.toString());
					return false;
				}
				logger.info("[成功]创建ftp目录：" + sbfDir.toString());
			}
			// 将目录切换至指定路径
			return ftp.changeWorkingDirectory(d);
		} catch (Exception e) {
			logger.info("创建并切换到指定目录路径 异常！dir:{}", dir, e);
			return false;
		}
	}

	private static String encodePath(String fileName) throws UnsupportedEncodingException {
		if (fileName == null) {
			return "";
		}

		return new String(fileName.getBytes("UTF-8"), "iso-8859-1");
	}

	/**
	 * 上传文件到远程ftp，FTPClient不会自动关闭
	 * 
	 * @param ftps
	 * @param remotePath 远程目录
	 * @param fileName   远程文件名
	 * @param fileStream 本地流数据
	 * @return
	 * @throws Exception
	 */
	public static boolean uploadFile(FTPClient ftps, String remotePath, String fileName, InputStream fileStream)
			throws Exception {
		return uploadFile(ftps, remotePath, fileName, fileStream, false);
	}

	public static boolean uploadFile(FTPClient ftps, String remotePath, String fileName, String localFilePath)
			throws Exception {
		FileInputStream fileStream = new FileInputStream(localFilePath);
		try {
			return uploadFile(ftps, remotePath, fileName, fileStream, false);
		} finally {
			fileStream.close();
		}
	}

	/**
	 * 上传文件到远程ftp，可以设置自动关闭FTPClient
	 * 
	 * @param ftps
	 * @param remoteDir          远程目录
	 * @param fileName           远程文件名
	 * @param fileStream         本地流数据
	 * @param autoCloseFTPClient 是否自动关闭ftpsclient
	 * @return
	 * @throws Exception
	 */
	public static boolean uploadFile(FTPClient ftps, String remoteDir, String fileName, InputStream fileStream,
			boolean autoCloseFTPClient) throws Exception {
		logger.info("文件上传开始，目录：{}，文件名：{}", remoteDir, fileName);
		boolean flag = false;
		try {
			flag = ftps.changeWorkingDirectory(encodePath(remoteDir));
			logger.debug("上传文件切换工作目录响应：{}，结果：{}", ftps.getReplyString(), flag);
			if (!flag) {
				flag = createDir(ftps, remoteDir);
				if (!flag) {
					logger.info("创建并切换目录响应：" + ftps.getReplyString());
					return flag;
				}
			}
			flag = ftps.storeFile(encodePath(fileName), fileStream);
			logger.info("上传文件响应：{}，{}", ftps.getReplyString(), flag);
		} finally {
			if (autoCloseFTPClient) {
				close(ftps);
			}
		}
		return flag;
	}

	public static List<String> list(FTPClient ftp, String remoteDir) {
		logger.info("获取指定目录：{}下的文件列表", remoteDir);
		List<String> list = Lists.newArrayList();
		try {
			boolean createSuccess = createDir(ftp, transformLinuxFileSeparator(remoteDir));
			if (createSuccess) {
				FTPFile[] fileArray = ftp.listFiles();
				for (FTPFile file : fileArray) {
					list.add(file.getName());
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("获取指定目录%s的文件列表 异常  ", remoteDir), e);
		}
		return list;
	}

	/**
	 * 上传文件
	 * 
	 * @param ftps
	 * @param remoteFile
	 * @param fileData
	 * @return
	 */
	public static boolean uploadFilePlainText(FTPClient ftps, String remoteFile, String fileData) {
		logger.debug("uploadFilePlaintext开始. remoteFile:{}", remoteFile);
		boolean uploadSuccess = false;
		try {
			boolean createSuccess = createDir(ftps, transformLinuxFileSeparatorForFile(remoteFile));
			if (createSuccess) {
				byte[] bytes = StringUtils.trimToEmpty(fileData).getBytes(UTF8);
				uploadSuccess = ftps.storeFile(new File(remoteFile).getName(), new ByteArrayInputStream(bytes));
			} else {
				logger.debug("uploadFilePlaintext创建目录 失败! {}", ftps.getReplyString());
			}
		} catch (Exception e) {
			logger.debug("uploadFilePlaintext上传异常，remoteFile:{}", remoteFile, e);
		}
		logger.debug("uploadFilePlaintext上传结束. 文件:{}, 状态:{}", remoteFile, uploadSuccess);
		return uploadSuccess;
	}

	/**
	 * 上传文件到远程ftp，可以设置自动关闭FTPClient，远程文件名与本地文件名一致
	 * 
	 * @param ftps
	 * @param remoteDir          远程目录
	 * @param file               本地文件
	 * @param autoCloseFTPClient
	 * @return
	 * @throws Exception
	 */
	public static boolean uploadFile(FTPClient ftps, String remoteDir, File file, boolean autoCloseFTPClient) {
		logger.info("uploadFile begin. remotePath:{}, zipFile:{}", remoteDir, file.getName());
		try (FileInputStream fileStream = new FileInputStream(file)) {
			return uploadFile(ftps, remoteDir, file.getName(), fileStream, autoCloseFTPClient);
		} catch (Exception e) {
			throw new RuntimeException("文件上传失败", e);
		}
	}

	public static boolean downloadFromFtp(FTPClient client, String remoteFilePath, String localFilePath) {
		try {
			File remoteFile = new File(remoteFilePath);
			String remoteDir = remoteFile.getParent();
			String remoteFileName = remoteFile.getName();
			if (File.separator.equals("\\") && remoteDir.indexOf("\\") > -1) {// 远程路径为linux，本地为Windows时，路径 分隔符需要替换一下
				remoteDir = remoteDir.replace("\\", "/");
			}
			boolean flag = client.changeWorkingDirectory(encodePath(remoteDir));
			if (!flag) {
				throw new RuntimeException(String.format("切换工作目录“%s”失败：%s", remoteDir, client.getReplyString()));
			}
			File f = new File(localFilePath);
			if (!f.exists()) {
				f.getParentFile().mkdirs();
			}
			OutputStream is = new FileOutputStream(localFilePath);
			// 2.保存本地临时文件
			boolean success = client.retrieveFile(encodePath(remoteFileName), is);
			is.flush();
			is.close();
			return success;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 下载并返回流数据
	 * 
	 * @param client
	 * @param remoteFile
	 * @return
	 * @throws Exception
	 */
	public static ByteArrayInputStream downloadFromFtp(FTPClient client, String remoteFile) {
		String remoteDir = transformLinuxFileSeparatorForFile(remoteFile);
		boolean createSuccess = createDir(client, remoteDir);
		if (!createSuccess) {
			throw new RuntimeException(String.format("ftp创建文件目录%s 并切入失败!", remoteDir));
		}
		InputStream inputStream = null;
		try {
			inputStream = client.retrieveFileStream(new File(remoteFile).getName());
			if (inputStream == null) {
				throw new RuntimeException(
						String.format("下载文件%s 失败. ftp返回信息: %s", remoteFile, client.getReplyString()));
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, bos);

			return new ByteArrayInputStream(bos.toByteArray());
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(String.format("下载文件%s 失败. ftp返回信息: %s", remoteFile, client.getReplyString()));
		} finally {
			if (inputStream != null) {
				try {
					client.completePendingCommand(); // 一定要加结束，不然同客户端连接无法复用
				} catch (IOException e) {
					logger.error("client completePendingCommand fail", e);
				}
			}
		}
	}

	/*
	 * 远程路径为linux，本地为Windows时，路径 分隔符需要替换一下
	 */
	private static String transformLinuxFileSeparatorForFile(String remoteFile) {
		return transformLinuxFileSeparator(new File(remoteFile).getParent());
	}

	private static String transformLinuxFileSeparator(String remoteFile) {
		return File.separator.equals("\\") && remoteFile.indexOf("\\") > -1 ? remoteFile.replace("\\", LINUX_SEPARATOR)
				: remoteFile;
	}

}
