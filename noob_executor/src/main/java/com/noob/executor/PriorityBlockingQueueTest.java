package com.noob.executor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityBlockingQueueTest {

	public static void main(String[] args) {
		PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<Integer>();

		for (int i = 0; i < 15; i++) {
			queue.add(new Random().nextInt(20));
			System.out.println(Arrays.toString(queue.toArray()));
		}

	}
}
