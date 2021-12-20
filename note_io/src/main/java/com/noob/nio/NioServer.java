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
	private static ServerSocketChannel serverSocketChannel2 = null; 

	/**
	 * 
	 * 同一个端口只能被一个ServerSocketChannel绑定监听。 否则报错：
	 * <p>
	 * Exception in thread "main" java.net.BindException: Address already in use: bind
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(8080)); // ServerSocketChannel只能绑定一个端口
		//serverSocketChannel.bind(new InetSocketAddress(8080)); // ServerSocketChannel只能绑定一个端口
	
		serverSocketChannel.configureBlocking(false);// 如果一个 ServerSocketChannel 要注册到 Selector 中，那么 必须是非阻塞的

		log.info("服务器启动, 服务地址: " + serverSocketChannel.getLocalAddress());

		selector = Selector.open();
		// ServerSocketChannel只能对SelectionKey.OP_ACCEPT有效 见：ServerSocketChannel.validOps()
		SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // 将ServerSocketChannel注册到Reactor线程的多路复用器Selector上。一定要监听ACCEPT,不然socketChannel的连接事件都进不来。
		
		serverSocketChannel2 = ServerSocketChannel.open();
		serverSocketChannel2.bind(new InetSocketAddress(8090));
		serverSocketChannel2.configureBlocking(false);
		serverSocketChannel2.register(selector, SelectionKey.OP_ACCEPT);
		
		
		/*
		 * Thread thread = new Thread( () -> { try { while(true) { int count =
		 * selector.select(); System.out.println(count); } } catch (Exception e) {
		 * System.out.println(e.getMessage()); }} ) ; thread.start();
		 * thread.interrupt(); // 这种方法虽然可以打断selector.select() 但是 从此之后不再会阻塞了
		 */		// selector.wakeup(); // 唤醒1次 selector.select()
		
		new IOHandler(selector, true).exectue(selector);
	}

}
