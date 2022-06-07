package com.noob.executor;

import java.util.concurrent.Semaphore;

public class SemaphoreTest {

	public static void main(String[] args) {
		Semaphore Semaphore = new Semaphore(1);
		for(int i = 0 ;  i<2 ; i++) {
			int m = i;
			new Thread( () -> {
				
				try {
					Semaphore.acquire();
					System.out.println(Thread.currentThread().getName());
					if( m == 0) {
						Semaphore.release();

					} else {
						Thread.sleep(10000000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} ).start();
		}
	}

}
