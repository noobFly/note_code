package com.noob.bio;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 阻塞IO
 */
@Slf4j
public class BioClient {
	private static OutputStream outputStream = null;
	private static PrintWriter outputPrint = null;
	private static InputStream inputStream = null;
	private static Scanner inputScanner = null;
	private static Socket socket = null;

	/**
	 * 若服务端下线，则客户端报异常：连接拒绝
	 * <p>
	 * java.net.ConnectException: Connection refused: connect
	 */
	public static void main(String[] args) throws Exception {
		try {
			// 创建客户端Socket，指定连接服务器地址和端口
			socket = new Socket("localhost", 8080);
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
			//socket.setSoTimeout(2000);

			log.info("客户端启动:" + socket.getLocalSocketAddress());// 每一个客户端分配一个端口号

			// 获取输入流，并读取服务器端的响应
			inputStream = socket.getInputStream();
		    inputScanner = acceptResponse(inputStream);
			//	readBlock(inputStream);

			outputStream = socket.getOutputStream();// 字节输出
			outputPrint = new PrintWriter(outputStream);// 将输出流包装成打印流
			int waitTime = keepSendMsgToServer(socket, outputPrint);
			// int waitTime = sendMsgToServerOne(socket, outputPrint);

			Thread.sleep(waitTime);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeIo();
		}
	}

	/**
	 * 接收消息
	 * 不受SoSoTimeout影响
	 */
	private static Scanner acceptResponse(InputStream inputStream) {
		Scanner inputScanner = new Scanner(inputStream);
		Thread inputThread = new Thread(() -> {
			while (true) {
				if (inputScanner.hasNext()) { // 阻塞获取数据！！！
					// 要求服务端响应一定要输出一个换行！！ 否则虽然能读取响应，但无法解析判定line end！
					log.info(inputScanner.nextLine());
				}
			}

		});

		inputThread.setDaemon(true); // 守护线程设置前会判定线程是否Alive，是则抛出IllegalThreadStateException
		inputThread.start();
		return inputScanner;
	}

	/**
	 * 接收消息
	 * read操作受SoSoTimeout影响
	 */
	private static void readBlock(InputStream inputStream) throws IOException {
		BufferedInputStream br = new BufferedInputStream(inputStream);
		byte[] buffer = new byte[1024];
		int turns = 1;
		while (br.read(buffer) != -1) { //阻塞直到有数据到来!  可自定义读取缓存的begin和end: read(b, 11, 50); 并不会清空原有的数据!
			System.out.print(new String(buffer));
			turns++;
		}
	}



	/**
	 * 一直发
	 * 
	 * @return
	 */
	private static int keepSendMsgToServer(Socket socket, PrintWriter outputPrint) {
		// 获取输出流，定时向服务器端发信息
		Thread outputThread = new Thread(() -> {
			ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

			AtomicInteger time = new AtomicInteger(1);
			service.scheduleWithFixedDelay(() -> {
				if(!socket.isClosed()) { // 客户端不主动关闭会一直发送，但服务端已经关闭了对端，它将不再被服务端接收处理。
					String msg = "客户端" + socket.getLocalSocketAddress() + "的慰问" + time.intValue() + "\n";
					/*
					 * if (time.intValue() > 1) { msg = "【test】"; // 验证read()不会主动清空原有数据
					 * ,而readLine()会定位nextLine的起始索引 }
					 */
					log.info("发出信息 ->>>>" + msg);
					outputPrint.write(msg);
					// outputPrint.println();
					outputPrint.flush(); // 这里才能正式推，很关键！
					time.addAndGet(1);
				} else {
					System.out.println("sokcet 被关闭了！");
				}
			}, 0, 3, TimeUnit.SECONDS);

		});
		outputThread.start();
		return 1000000;
	}

	/**
	 * 发一次就关闭
	 * 
	 * @return
	 * @throws IOException
	 */
	private static int sendMsgToServerOne(Socket socket, PrintWriter outputPrint) {
		// 获取输出流，定时向服务器端发信息
		String msg = "客户端" + socket.getLocalSocketAddress() + "的慰问1";
		log.info("发出信息->>>>" + msg);
		outputPrint.write(msg);
		// outputPrint.println(); // 若服务端使用readLine, 一定要输出一个换行！！
		// 否则服务端虽然能读取客户端的输出流，但无法解析判定line end！
		outputPrint.flush();// 这里才能正式推 ，很关键！
		try {
			socket.shutdownOutput(); // 关闭输出流 ,后续再输出无效。
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 100;
	}

	private static void closeIo() throws IOException {
		if (outputPrint != null)
			outputPrint.close();
		if (outputStream != null)
			outputStream.close();
		if (inputStream != null)
			inputStream.close();
		if (inputScanner != null)
			inputScanner.close();
		if (socket != null)
			socket.close();
	}
}
