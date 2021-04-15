package com.noob.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 核心线程是通过size的变化处理的。 并不是首次创建的工作线程一定是最后存活下来的核心线程
 *
 */
public class TestThreadPool {
	private static AtomicInteger count = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(1));
		for (int i = 0; i < 3; i++) {

			try {
				executor.execute(() -> {
					System.out.println(Thread.currentThread().getId());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (count.getAndIncrement() == 0) {

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
			}
		}
		Thread.sleep(100);
		executor.execute(() -> {
			System.out.println(Thread.currentThread().getId());
		});
	}
}
