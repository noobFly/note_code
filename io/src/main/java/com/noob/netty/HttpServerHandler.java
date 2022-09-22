package com.noob.netty;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {

		if (msg instanceof HttpRequest) {
			// 响应HTML
			String responseHtml = "<html><body>Hello, " + "第一个输出 " + "</body></html>";
			byte[] responseBytes = responseHtml.getBytes("UTF-8");
			int contentLength = responseBytes.length;
			// 构造FullHttpResponse对象，FullHttpResponse包含message body
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					Unpooled.wrappedBuffer(responseBytes));
			response.headers().set("Content-Type", "text/html; charset=utf-8");
			response.headers().set("Content-Length", Integer.toString(contentLength));

			// 发送数据到客户端
			ChannelFuture cahnnelFuture = ctx.writeAndFlush(response);
			cahnnelFuture.addListener(new ChannelFutureListener() {
				// write操作完成后的回调
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						System.out.println("write complete success");
					} else {
						System.out.println("write complete fail");

					}
				}
			});
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
