package com.noob.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// 这些个线程里抛出的异常默认只会输出到控制台。若要输出到日志文件需要手动指明
public class ExceptionThread implements Runnable {

	@Override
	public void run() {
		throw new RuntimeException("这个线程就干了这么一件事，抛出一个运行时异常");
	}

	// 会输出2次异常日志。 所以在线程中抛出异常不影响该线程处理下次任务
	public static void main(String[] args) throws InterruptedException {
		try {
			ExecutorService exec = Executors.newFixedThreadPool(1);

			exec.execute(new ExceptionThread());
			log.info("该干嘛干嘛去");
			exec.execute(new ExceptionThread());
			log.info("该干嘛干嘛去");

			Future<String> future = exec.submit(() -> {
				throw new RuntimeException("test callable exception");
			});
			try {
				future.get();// FutureTask.get() 将执行排除的异常包装成ExecutionException抛出
			} catch (Exception e) {
				log.error("", e);
				throw e;
			}
		} catch (Exception e) {
			log.info("捕获到异常");
			RuntimeException exception = new RuntimeException("捕获再抛出", e);
			exception.addSuppressed(e);
			throw exception;
		}
		Thread.sleep(100000L);

	}

}