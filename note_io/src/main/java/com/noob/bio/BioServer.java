package com.noob.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;
/**
 * 总结： 两次阻塞 1、等待连接 2、 等待数据读取
 * <p>
 * 1. 单线程处理的阻塞是由于单线程导致的，与阻塞IO的真实原因不是一个概念。
 * <p>
 * 2. InputStream中读取数据的方式要与OutputStream中的输出数据格式相匹配才能有效快速的解析出数据（eg. 是否需要\n）;
 * <p>
 * 读取方式可以不同，但相同的是解析不到数据时阻塞； 不同读取方式在客户端socket正常关闭OutputStream与非正常断开的呈现不一样，
 * 具体参看ClientRequestHandler中处理reqesut. 推荐使用handleWithoutLineBreak();
 * <p>
 * 3. OutputStream：
 * 在buffer中，如果消息的字节小于buffer容量，是不会立刻推送的，只有输出流中的缓冲区填满时，输出流才真正推送消息。
 * <p>
 * flush()方法可以强迫输出流(或缓冲的流)发送数据，即使此时缓冲区还没有填满 。close() 关闭输出流时存储在输出流的缓冲区中的数据就会丢失
 * 所以关闭(close)输出流时，应先刷新(flush)换冲的输出流：“迫使所有缓冲的输出数据被写出到底层输出流中”
 * <p>
 * 4. shutdownOutput()后再write是无效的.
 * <p>
 * 参考 https://blog.csdn.net/mijichui2153/article/details/80969404?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task

 */
@Slf4j
public class BioServer {

	/**
	 * 
	 * 同一个端口只能被一个ServerSocket绑定监听。 否则报错：
	 * <p>
	 * Exception in thread "main" java.net.BindException: Address already in use:
	 * JVM_Bind
	 */
	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = new ServerSocket(8080);
		log.info("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

		while (true) {
			Socket clientSocket = serverSocket.accept(); // 等待客户端连接--阻塞!!!  在第三次握手之后 或则 第三次握手将数据和ack一起返回
			clientSocket.setSoTimeout(3000); // 与此 Socket 关联的 SocketInputStream#read() 将阻塞至该事件，  如果超过超时值，该Socket将引发 java.net.SocketTimeoutException

			/**
			 * java.net.SocketTimeoutException: Read timed out
			 * 	at java.net.SocketInputStream.socketRead0(Native Method)
			 * 	at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
			 * 	at java.net.SocketInputStream.read(SocketInputStream.java:171)
			 * 	at java.net.SocketInputStream.read(SocketInputStream.java:141)
			 * 	at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
			 * 	at java.io.BufferedInputStream.read1(BufferedInputStream.java:286)
			 * 	at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
			 * 	at java.io.FilterInputStream.read(FilterInputStream.java:107)
			 */


			clientSocket.setTcpNoDelay(true);
			clientSocket.setKeepAlive(true);
			
			// requestHandlerAsync(clientSocket);
			requestHandler(clientSocket); // 可选多线程处理
		}
	}

	/**
	 * 单线程执行，客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞.
	 */
	private static void requestHandler(Socket clientSocket) throws IOException {
		new RequestHandler(clientSocket).run();
	}

	private static void requestHandlerAsync(Socket clientSocket) throws IOException {
		new Thread(new RequestHandler(clientSocket)).start();
	}
}
