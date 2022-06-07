package com.noob.executor;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolTest {
	static int max = Integer.MAX_VALUE >> 2;

	public static void main(String[] args) throws Exception {
		testHasResultTask();
	}

	public static void testHasResultTask() throws Exception {

		Instant now = Instant.now();
		int result1 = 0;
		for (int i = 1; i <= max; i++) {
			result1 += i;
		}

		System.out
				.println(" 循环计算 1-1000000 累加值：" + result1 + "  耗时: " + Duration.between(now, Instant.now()).toMillis());

		ForkJoinPool pool = new ForkJoinPool(15);
		Instant now2 = Instant.now();

		ForkJoinTask<Integer> task = pool.submit(new CalculateTask(1, max));
		int result2 = task.get();
		System.out.println(
				" 并行计算 1-1000000 累加值：" + result2 + "  耗时: " + Duration.between(now2, Instant.now()).toMillis());
		pool.awaitTermination(2, TimeUnit.SECONDS);
		pool.shutdown();
	}
}