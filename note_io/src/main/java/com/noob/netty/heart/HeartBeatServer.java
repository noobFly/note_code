package com.noob.netty.heart;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * IdleStateHandler：
 * readerIdleTime读空闲超时时间设定，如果channelRead()方法超过readerIdleTime时间未被调用则会触发超时事件调用userEventTrigger()方法；
 * 
 * writerIdleTime写空闲超时时间设定，如果write()方法超过writerIdleTime时间未被调用则会触发超时事件调用userEventTrigger()方法；
 * 
 * allIdleTime所有类型的空闲超时时间设定，包括读空闲和写空闲；
 * 
 * @author admin
 *
 */

public class HeartBeatServer {

	public static void main(String[] args) throws Exception {
		ServerBootstrap bootstrap = new ServerBootstrap();

		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			bootstrap.group(boss, worker).handler(new LoggingHandler(LogLevel.INFO))
					.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							//不同的SocketChannel互不影响
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast("decoder", new StringDecoder());
							pipeline.addLast("encoder", new StringEncoder());
							pipeline.addLast(new IdleStateHandler(10, 10, 15, TimeUnit.SECONDS)); // 心跳检测处理器
							pipeline.addLast(new HeartBeatHandler()); // 要在IdleStateHandler之后
						}
					});

			ChannelFuture future = bootstrap.bind(8090).sync();
			ChannelFuture future2 = bootstrap.bind(80920).sync();

			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			worker.shutdownGracefully();
			boss.shutdownGracefully();
		}
	}
}
