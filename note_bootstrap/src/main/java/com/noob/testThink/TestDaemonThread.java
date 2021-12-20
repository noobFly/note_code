package com.noob.testThink;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// main函数就是个非守护线程，虚拟机的gc就是一个守护线程。
//只要有任何非守护线程还没有结束，java虚拟机的实例都不会退出!!  所以即使main函数这个非守护线程退出，但是由于在main函数中启动的匿名线程也是非守护线程，它还没有结束，所以jvm没办法退出。

public class TestDaemonThread {

	public static void main(String[] args) {
		
			String  s = "a";
			
	        switch (s) {
			case "a": //a分支
				 System.out.println("匹配成功1");
				
				    
			case "b": //b分支
			        System.out.println("匹配成功2");
			        return;
			case "c": //c分支
			         System.out.println("匹配成功3");
			         break;
			case "d": //d分支
			         System.out.println("匹配成功4");
			         break;
			default:
		         System.out.println("匹配成功default");
				break;
			}
		
		ExecutorService service =	Executors.newCachedThreadPool();
		for (int i = 0; i < 3; i++) {
			service.submit(()->{
				System.out.println(Thread.currentThread().getId());
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
		}
		
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
