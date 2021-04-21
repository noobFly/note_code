package com.noob.servlet;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
//在service方法中业务逻辑如果碰到io操作时间比较长的操作，就会长时间占用tomcat容器线程池中的线程，肯定会阻塞线程处理其他请求。
//引入异步servlet的目的就是将容器线程池和业务线程池分离开。在处理大io的业务操作的时候，  把这个操作移动到业务线程池中进行，释放容器线程，使得容器线程更多的接收处理外部IO请求，
// 在业务逻辑执行完毕之后，然后在通知tomcat容器线程池来继续后面的操作，这个操作应该是把处理结果commit到客户端或者是dispatch到其他servlet上。

@WebServlet(urlPatterns = "/async", asyncSupported = true)
@Slf4j
public class AsyncServlet extends HttpServlet {
   // 将业务操作交给应用线程， tomcat线程池可以有更多的资源去处理IO请求响应相关的事情
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("step into AsyncServlet");
		// step1: 开启异步上下文
		final AsyncContext ctx = req.startAsync();
		ctx.getResponse().getWriter().print("async servlet1");
		ctx.getResponse().getWriter().flush(); 
		resp.getWriter().print("async servlet2");
		resp.getWriter().flush();
		// 并不会立即输出。也是等到ctx.complete()完成 一并输出
		// step2: 提交线程池异步执行
		executorService.execute(() -> {
			try {
				log.info("async SocketEvent.OPEN_READ 准备执行了");
				// 模拟耗时
				Thread.sleep(1000L);
				ctx.getResponse().getWriter().print("async servlet3");
				log.info("async SocketEvent.OPEN_READ 执行了");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// step3: 完成回调输出。
			ctx.complete();
		});
	}

}