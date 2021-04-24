package com.noob.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NioServer {
	private static ServerSocketChannel serverSocketChannel = null; // 用于监听客户端的连接，所有客户端连接的父管道
	private static Selector selector = null; // 多路复用器

	/**
	 * 
	 * 同一个端口只能被一个ServerSocketChannel绑定监听。 否则报错：
	 * <p>
	 * Exception in thread "main" java.net.BindException: Address already in use: bind
	 * 
	 */
	public static void main(String[] args) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(8080)); // ServerSocketChannel只能绑定一个端口

		serverSocketChannel.configureBlocking(false);// 如果一个 ServerSocketChannel 要注册到 Selector 中，那么 必须是非阻塞的

		log.info("服务器启动, 服务地址: " + serverSocketChannel.getLocalAddress());

		selector = Selector.open();
		// ServerSocketChannel只能对SelectionKey.OP_ACCEPT有效 见：ServerSocketChannel.validOps()
		SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // 将ServerSocketChannel注册到Reactor线程的多路复用器Selector上。一定要监听ACCEPT,不然socketChannel的连接事件都进不来。
		new IOHandler(selector, true).exectue();
	}

}
