package com.noob.test;

import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.noob.testProtected.other.C;
import com.noob.testThink.Storage;

/**
 * 核心线程是通过size的变化处理的。 并不是首次创建的工作线程一定是最后存活下来的核心线程
 *
 */
public class TestThreadPool extends Storage {
	private static AtomicInteger count = new AtomicInteger(0);

	private int f = 100;
	public TestThreadPool() {
		f = 1000;
	}
	
	public static void main(String[] args) throws Exception {
			new TestThreadPool();

		System.out.println(testTryCatch());

		int size = 10;
		int s = --size;
		s = size--;
		PriorityQueue<Integer> q = new PriorityQueue<Integer>(); // 阉割版的最小堆排序， 只保证 parent < left and parent < right ,
		q.offer(1);
		q.offer(10);
		q.offer(5);
		q.offer(13);
		q.offer(13);
		q.offer(13);
		q.offer(9);
		q.offer(2);
		q.offer(11); // [1, 2, 5, 10, 13, 13, 9, 13, 11]
		Integer b = q.poll();
		
		
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

	private static int testTryCatch() throws Exception {
		int a = 2;
		try {
			System.out.println("try");
			if (a == 2) {
				throw new Exception();
			}
			return 1;

		} catch (Exception e) {
			System.out.println("exception");
			throw e;
			// return 3;
		} finally {
			System.out.println("finally");
			//return 2; // 此时会直接返回2 ， try和catch里的return都不会执行
		}
	}
}
