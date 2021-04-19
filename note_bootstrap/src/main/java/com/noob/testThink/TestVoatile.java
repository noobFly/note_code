package com.noob.testThink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * volidate 也保证不了原子性 .
 * 也就只是for循环unsafe.compareAndSwapInt能出来。并通过unsafe.getIntVolatile获取 long
 * 只是涉及到底层cpu指令读写才是原子性（32位系统处理64位数据分为2次操作）
 * 
 * @param args
 * @throws InterruptedException
 */
public class TestVoatile {
	private int i = 0;
	private volatile int j = 0;
	static AtomicInteger m = new AtomicInteger(0);
	private static volatile long l = 0;

	public static void main(String[] args) throws InterruptedException {
		ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>(2);
		new Thread(() -> {
			for (int i = 0; i < 100000; i++) {
				if(i == 2) {
					try {
						Thread.sleep(100000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				map.put(i, i);
			}
		}).start();

		for (int i = 0; i < 10000000; i++) {
			map.put(i, i);
		}
	

		TestVoatile obj = new TestVoatile();
		CountDownLatch lock = new CountDownLatch(2);
		new Thread(() -> {
			for (int a = 0; a < 100000; a++) {
				obj.i++;
				obj.j++;
				m.incrementAndGet();
				l++;
			}
			lock.countDown();

		}).start();

		new Thread(() -> {
			for (int a = 0; a < 100000; a++) {
				obj.i++;
				obj.j++;
				m.incrementAndGet();
				l++;
			}
			lock.countDown();

		}).start();

		lock.await();
		System.out.println(obj.i);
		System.out.println(obj.j);
		System.out.println(m.get());
		System.out.println(l);

	}

}
