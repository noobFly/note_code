package com.noob.testThink;

// main函数就是个非守护线程，虚拟机的gc就是一个守护线程。
//只要有任何非守护线程还没有结束，java虚拟机的实例都不会退出!!  所以即使main函数这个非守护线程退出，但是由于在main函数中启动的匿名线程也是非守护线程，它还没有结束，所以jvm没办法退出。

public class TestDaemonThread {

	public static void main(String[] args) {
		Thread a = new Thread(() -> {
			while (true) {
				System.out.println("daemon");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		a.setDaemon(true);
		a.start();

		new Thread(() -> {
			while (true) {
				System.out.println("this is b");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start(); // 只要该线程没挂，daemon就一直执行！
	}
}
