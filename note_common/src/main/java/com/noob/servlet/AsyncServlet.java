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

@WebServlet(urlPatterns = "/async", asyncSupported = true)
@Slf4j
public class AsyncServlet extends HttpServlet {

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