package com.noob.netty.heart;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class HeartBeatClient {

	public static void main(String[] args) throws Exception {
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new HeartBeatClientInitializer());

			bootstrap.connect("localhost", 8090).sync().channel();
			while (true) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	static class HeartBeatClientInitializer extends ChannelInitializer<Channel> {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			// 要注意处理类的顺序！
			pipeline.addLast("decoder", new StringDecoder());
			pipeline.addLast("encoder", new StringEncoder());
			pipeline.addLast(new IdleStateHandler(10, 5, 10, TimeUnit.SECONDS), new HeartBeatClientHandler()); // HeartBeatClientHandler要在IdleStateHandler之后

		}
	}

	static class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {

		private int count;

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
			System.out.println(" ====== > [client message received ]  : " + msg);
			if (msg != null && msg.equals("you are out")) {
				System.out.println(" server closed connection , so client will close too");
				ctx.channel().closeFuture();
			}
		}

		// 服务端判定心跳超时后，关闭通道
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			IdleStateEvent event = (IdleStateEvent) evt;

			String eventType = null;
			switch (event.state()) {
			case READER_IDLE:
				eventType = "读空闲";
				break;
			case WRITER_IDLE:
				eventType = "写空闲";
				// 不处理
				break;
			case ALL_IDLE:
				eventType = "读写空闲";
				// 不处理
				break;
			}
			System.out.println(ctx.channel().remoteAddress() + "超时事件：" + eventType);
			ctx.channel().writeAndFlush("发起 ping  " + count +  " " + eventType);
			count++;

		}
	}
}
