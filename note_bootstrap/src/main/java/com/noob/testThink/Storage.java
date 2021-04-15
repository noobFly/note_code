package com.noob.testThink;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Storage {

	private int capacity = 10;
	private Queue<Object> queue = new LinkedList<Object>();
	final ReentrantLock lock = new ReentrantLock();

	/** 当前队列不是空的，可以消费 */
	private final Condition notEmpty = lock.newCondition();

	/** 当前队列不是满的， 可以生产 */
	private final Condition notFull = lock.newCondition();

	private Object obj = new Object();

	public void produce2() {
		synchronized (obj) {
			try {
				while (queue.size() > capacity - 1) {
					System.out.println("库存量:" + queue.size() + "已满, 暂时不能执行生产任务!");
					obj.wait();
				}
				queue.add(new Object());
				System.out.println("生产了, 现仓储量为:" + queue.size());
				obj.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void consume2() {
		synchronized (obj) {
			try {
				lock.lockInterruptibly();
				while (queue.size() == 0) {
					System.out.println("库存量" + queue.size() + "暂时不能执行消费任务!");
					obj.wait();
				}
				queue.remove();
				System.out.println("消费了, 现仓储量为:" + queue.size());
				obj.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void produce() {
		final ReentrantLock lock = this.lock;
		try {
			lock.lockInterruptibly();
			while (queue.size() > capacity - 1) {
				System.out.println("库存量:" + queue.size() + "已满, 暂时不能执行生产任务!");
				notFull.await();
			}
			queue.add(new Object());
			System.out.println("生产了, 现仓储量为:" + queue.size());
			notEmpty.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void consume() {
		final ReentrantLock lock = this.lock;
		try {
			lock.lockInterruptibly();
			while (queue.size() == 0) {
				System.out.println("库存量" + queue.size() + "暂时不能执行消费任务!");
				notEmpty.await();
			}
			queue.remove();
			System.out.println("消费了, 现仓储量为:" + queue.size());
			notFull.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Storage storage = new Storage();
		new Thread(() -> {
			while (true) {
				storage.produce2();
				try {
					Thread.sleep(1000); // 执行太快了降速
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();

		new Thread(() -> {
			while (true) {
				storage.consume2();
				try {
					Thread.sleep(1000);// 执行太快了降速
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
