package com.noob.test.zookeeper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.noob.zookeeper.lock.DistributedLock;

public class DistributedLockTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private CountDownLatch startSignal = new CountDownLatch(1);//开始阀门，限制所有的任务线程必须同时开始。

	private CountDownLatch doneSignal = null;//结束阀门
	private AtomicInteger exceptionTotal = new AtomicInteger();
	private CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<>();

	private Runnable[] task = null;

	public DistributedLockTest(Runnable... task) {
		this.task = task;
		if (task == null) {
			logger.error("task must be not null!");
			System.exit(1);
		}
		doneSignal = new CountDownLatch(task.length);
		start();
	}

	private void start() {
		executeTask();
		calculateTime();
	}

	/**
	 * 初始化所有线程，并在阀门处等待
	 */
	private void executeTask() {
		long len = doneSignal.getCount();
		for (int i = 0; i < len; i++) {
			final int j = i;
			new Thread(() -> {
				try {
					startSignal.await();//使当前线程在锁存器倒计数至零之前一直等待
					Instant start = Instant.now();
					task[j].run();
					list.add(Duration.between(start, Instant.now()).toMillis());
				} catch (Exception e) {
					exceptionTotal.getAndIncrement();
				}
				doneSignal.countDown();
			}).start();
		}
	}

	/**
	 * 计算响应时间
	 */
	private void calculateTime() {
		startSignal.countDown(); //打开阀门
		try {
			doneSignal.await();//等待所有线程都执行完毕
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		//计算执行时间
		getExeTime();
	}

	/**
	 * 计算平均响应时间
	 */
	private void getExeTime() {
		int size = list.size();
		List<Long> _list = new ArrayList<>(size);
		_list.addAll(list);
		DoubleSummaryStatistics statistics =
				list.stream().collect(Collectors.summarizingDouble(Long::doubleValue));

		System.out.println("min: " + statistics.getMin());
		System.out.println("max: " + statistics.getMax());
		System.out.println("avg: " + statistics.getAverage());
		System.out.println("err: " + exceptionTotal.get());
	}

	public static void main(String[] args) {

		Runnable[] tasks = new Runnable[10];
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = () -> {
				DistributedLock lock = null;
				try {
					lock = DistributedLock.blockInstance("/noob", "contract");
					lock.lock();
					System.out.println("Thread " + Thread.currentThread().getId() + " running");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					lock.unlock();
				}

			};
		}
		new DistributedLockTest(tasks);
	}
}
