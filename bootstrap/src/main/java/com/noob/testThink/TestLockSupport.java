package com.noob.testThink;

import java.util.concurrent.locks.LockSupport;

public class TestLockSupport {

	public static void main(String[] args) throws InterruptedException {
		Thread a = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(i);
				LockSupport.park(Thread.currentThread()); // 只能挂起当前线程
				System.out.println(i);

			}
		});
		a.start();
		LockSupport.unpark(a); // 可以unpark指定线程, 线程需要start()

	}
}
