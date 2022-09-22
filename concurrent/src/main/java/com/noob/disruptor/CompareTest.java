package com.noob.disruptor;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * 担心影响， 分开执行测试
 * 
 * @author admin
 *
 */
public class CompareTest {
	public static int THREAD = 2; // 线程数量
	public static int PER = 2; // 单个线程生产数量
	public static int TOTAL_COUNT = THREAD * PER; // 数据总量
	public static int SIZE = 1; // 最大容量

	public static void main(String[] args) {
		println("线程数：" + THREAD + " 单线程生产量: " + PER + " 容量：" + SIZE + " 数据总量：" + TOTAL_COUNT);
		// new Thread(() -> ArrayBlockingQueueTest.execute()).start();
		 DisruptorTest.execute();
	}

	public static void println(String msg) {
		System.out.println(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + "[" + Thread.currentThread().getName() + "] " + msg);
	}
}
