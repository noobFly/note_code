package com.noob.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 响应式编程是一种异步编程范式， 范例通常以面向对象的语言表示，作为Observer设计模式（观察者模式）的扩展。
 * <p>
 * 响应式编程之Reactor 事件驱动模式 是JVM的完全非阻塞反应式编程基础类库。
 * 它直接与Java8功能的API集成，特别是CompletableFuture，Stream和Duration。
 * 在响应式编程里，是Publisher在新的可用值出现时通知Subscriber，而此推送模式是做出反应的关键。(通知Subscriber就像推送消息，所以叫push，在迭代器中，是迭代器主动获取下一个迭代值，就像拉取值，所以是pull)。同样，应用于推送值的操作，是以声明方式而不是命令方式
 **/
public class HttpServer {

	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 实际上是一个线程组，可以通过构造方法设置线程数量，默认为CPU核心数*2。boss用于服务器接收新的TCP连接，boss线程接收到新的连接后将连接注册到worker线程。worker线程用于处理IO操作，例如read、write。
		EventLoopGroup workerGroup = new NioEventLoopGroup(); // 一个 NioEventLoop 维护了一个 Selector（使用的是 Java 原生的 Selector： SelectorProvider.provider().openSelector()）
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new HttpServerCodec()); // http 编解码
							pipeline.addLast("httpAggregator", new HttpObjectAggregator(512 * 1024)); // http 消息聚合器512*1024为接收的最大contentlength . http消息在传输的过程中可能是一片片的消息片端，就需要HttpObjectAggregator来把它们聚合起来。
							pipeline.addLast(new HttpServerHandler());
						}
					});
			ChannelFuture f = b.bind(8080).sync(); // 每绑定1个端口， 就会创建1个NioServerSocketChannel去监听端口事件， 同时也会从master上分配1个NioEventLoop去处理。
			ChannelFuture f2 = b.bind(8082).sync();// 同一个端口只能被绑定一次，但可以绑定多个端口。

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}

/**
 * Netty和Tomcat最大的区别就在于通信协议，Tomcat是基于Http协议的，他的实质是一个基于http协议的web容器。
 * 多路选择复用器可以管理更多的sokcet监听事件。
 * <p>
 * 但是Netty不一样，他能通过编程自定义各种协议，因为netty能够通过codec自己来编码/解码字节流。
 * <p>
 * 如果只监听一个端口号，那么只需要一个boss线程即可，推荐将bossGroup的线程数量设置成1。
 * 当有新的TCP客户端连接到服务器，将由boss线程来接收连接，然后将连接注册到worker线程，当客户端发送数据到服务器，worker线程负责接收数据，并执行ChannelPipeline中的ChannelHandler。
 * <p>
 * 如果有耗时的业务逻辑阻塞住worker线程，例如在channelRead中执行一个耗时的数据库查询，会导致IO操作无法进行，服务器整体性能就会下降。一定要另外开启应用线程来处理！
 */

// https://www.cnblogs.com/wang-meng/p/13557635.html