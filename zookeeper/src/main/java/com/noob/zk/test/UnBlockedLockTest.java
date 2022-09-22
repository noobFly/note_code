package com.noob.zk.test;

import com.noob.zk.lock.UnBlockedLock;


public class UnBlockedLockTest {

	public static void main(String[] args) {

		try {
			UnBlockedLock lock = new UnBlockedLock("/noob", "contract");
			lock.lock();
			System.out.println("Thread " + Thread.currentThread().getId() + " running");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {

		}
	}
}
