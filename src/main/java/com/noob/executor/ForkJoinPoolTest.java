package com.noob.executor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StopWatch;

public class ForkJoinPoolTest {
 static int max = 1000000000;
	public static void main(String[] args) throws Exception {
		testHasResultTask();
	}

	public static void testHasResultTask() throws Exception {
		StopWatch sw = new StopWatch("计时");
		int result1 = 0;
		sw.start("循环计算");
		for (int i = 1; i <= max; i++) {
			result1 += i;
		}
		System.out.println(" 循环计算 1-1000000 累加值：" + result1);
		sw.stop();

		ForkJoinPool pool = new ForkJoinPool();
		sw.start("并行计算");
		ForkJoinTask<Integer> task = pool.submit(new CalculateTask(1, max));
		int result2 = task.get();
		sw.stop();
		System.out.println(" 并行计算 1-1000000 累加值：" + result2);
		pool.awaitTermination(2, TimeUnit.SECONDS);
		pool.shutdown();
		//查看所有业务的耗时统计情况
        String result = sw.prettyPrint();
        System.out.println(result);
	}
}