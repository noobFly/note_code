package com.noob.test;

import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.noob.loadBalance.LoadBalanceSpi;
import com.noob.loadBalance.impl.ConsistentHashLoadBalance;
import com.noob.loadBalance.impl.RandomLoadBalance;
import com.noob.loadBalance.impl.RoundRobinLoadBalance;
import com.noob.loadBalance.impl.ServiceInvoker;

public class TestLoadBalance {

	static String[] ServiceIps = new String[] { "127.0.0.1:8080", "189.84.255.216:8080", "172.164.11.144:8080" };

	public static void main(String[] args) {
		 //testRandomLoadBalance();
		//testConsistentHashLoadBalance();
		//testRoundRobinLoadBalance();
		// testTreeMap();
		// testRandom();
		  testSPI();

	}
   
	private static void testSPI() {
		LoadBalanceSpi.spiInfo();
	}

	private static void testRandomLoadBalance() {
		RandomLoadBalance.sortByTreeMap();
		RandomLoadBalance.sortInt();
	}

	private static void testConsistentHashLoadBalance() {
		ConsistentHashLoadBalance loadBalance = new ConsistentHashLoadBalance();
		Object[][] params = new Object[][] { { "noticeID_2354", "WEIXIN", "10", "100" },
				{ "interestID_1313", "WEIBO", "3", "20" }, { "historyID_9876", "ZHIHU", "2", "10" },
				{ "forkID_5476", "DOUXIN", "29", "10" } };

		String[] methodNames = new String[] { "queryNotice", "queryInterest", "queryHistory", "queryFork" };
		for (int j = 0; j < params.length; j++) {
			List<ServiceInvoker> invokerList = Lists.newArrayList();

			for (int i = 0; i < ServiceIps.length; i++) {
				invokerList.add(ServiceInvoker.builder().serviceIp(ServiceIps[i]).methodName(methodNames[j])
						.hashArguments("0,1").applicationName("testDubbo").build()); // 同一次的loadBalance的多个invokers除服务提供者不一样，其他的应该是相同的
			}

			System.out.println(loadBalance.select(invokerList, params[j]).getServiceIp());
		}

	}

	private static void testRoundRobinLoadBalance() {
		RoundRobinLoadBalance loadBalance = new RoundRobinLoadBalance();
		for (int j = 0; j < 100; j++) {
			List<ServiceInvoker> invokerList = Lists.newArrayList();

			for (int i = 0; i < ServiceIps.length; i++) {
				invokerList.add(ServiceInvoker.builder().serviceIp(ServiceIps[i]).methodName("queryNotice")
						.hashArguments("0,1").applicationName("testDubbo").weight(ServiceIps.length - i).build());
			}

			System.out.println(loadBalance.select(invokerList, null).getServiceIp());
		}

	}

	/**
	 * ceilingKey & ceilingEntry : 优先返回指定key, 若没有返回大于且最近的key。如果超出最大key值， 返回null.
	 * <p>
	 * tailMap : ture or false 标识决定是否返回相等Key的项
	 */
	private static void testTreeMap() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		map.put(2, "F1");
		map.put(8, "F3");
		map.put(5, "F2");

		System.out.println(map.ceilingEntry(1)); // 2=F1
		System.out.println(map.ceilingEntry(2)); // 2=F1
		System.out.println(map.ceilingEntry(16)); // null
		System.out.println(map.ceilingEntry(6)); // 8=F3

		System.out.println(map.tailMap(2, false).firstEntry()); // 5=F2
		System.out.println(map.tailMap(-2, true).firstEntry()); // 2=F1
		System.out.println(map.tailMap(8, false).firstEntry()); // null
		System.out.println(map.tailMap(8, true).firstEntry()); // 8=F3

		System.out.println(map.tailMap(16, true).firstEntry()); // null
		System.out.println(map.tailMap(16, false).firstEntry()); // null

	}

	/**
	 * Random类中的nextInt(n)系列方法生成[0, n)的伪随机数;; nextInt()方法会产生所有(32位)有效的整数，所以会有负数。
	 * <p>
	 * Math.random() 的范围是 [0, 1);
	 */
	private static void testRandom() {
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			System.out.println(random.nextInt()); // 出现负数
		}
		for (int i = 0; i < 100; i++) {
			System.out.println(Math.random()); // [0, 1)
		}
	}
}
