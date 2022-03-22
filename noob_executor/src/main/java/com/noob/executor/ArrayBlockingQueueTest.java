package com.noob.executor;

import com.noob.disruptor.CompareTest;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ArrayBlockingQueueTest {

	public static void execute() {
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		AtomicBoolean endP = new AtomicBoolean(false);
		AtomicBoolean endC = new AtomicBoolean(false);
		long startTime = System.currentTimeMillis();
		AtomicLong count = new AtomicLong(0);
		for (int i = 0; i < CompareTest.THREAD; i++) {
			final int m = i;
			new Thread(() -> {
				for (int j = 0; j < CompareTest.PER; j++) {
					try {
						queue.put("i" + m + "j" + j); // 队列不够，等待生产
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (count.incrementAndGet() == CompareTest.TOTAL_COUNT) {
						CompareTest.println("ArrayBlockingQueue 生产耗时：" + (System.currentTimeMillis() - startTime));
						endP.set(true);
					}
				}
			}).start();
		}

		new Thread(() -> {
			AtomicLong consumerCount = new AtomicLong(0);
			while (true) {
				try {
					queue.take(); // 直到消费完所有信息
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (consumerCount.incrementAndGet() == CompareTest.TOTAL_COUNT) {
					break;
				}
			}
			CompareTest.println("处理count：" + consumerCount.get() + "  ArrayBlockingQueue 消费耗时："
					+ (System.currentTimeMillis() - startTime));
			endC.set(true);
		}).start();

		while (!(endC.get() && endP.get())) {

		}
		CompareTest.println("ArrayBlockingQueue 总耗时：" + (System.currentTimeMillis() - startTime));

	}
}
