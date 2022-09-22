package com.noob.loadBalance.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.noob.loadBalance.AbstractLoadBalance;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 
 * 轮询负载算法
 * <p>
 * 与RandomLoadBalance的核心处理逻辑是一样的
 * <p>
 * 不同的是： 轮询负载算法的基准数是访问次数累加AtomicInteger, 而随机负载算法则是一个随机数字
 * <p>
 * 需要注意权重表示范围的边界
 * 
 * @author admin
 *
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

	private final ConcurrentMap<String, AtomicInteger> sequences = new ConcurrentHashMap<String, AtomicInteger>();

	@Override
	protected ServiceInvoker doSelect(List<ServiceInvoker> invokers, Object[] parameters) {
		int length = invokers.size();
		int maxWeight = 0; // 最大权重
		int minWeight = Integer.MAX_VALUE; // 最小权重
		final LinkedHashMap<ServiceInvoker, IntegerWrapper> invokerToWeightMap = new LinkedHashMap<ServiceInvoker, IntegerWrapper>(); // linkedHashMap保证服务列表有序
		int weightTotal = 0; // 权重累加值
		for (int i = 0; i < length; i++) {
			ServiceInvoker serviceInvoker = invokers.get(i);
			int weight = serviceInvoker.getWeight();
			maxWeight = Math.max(maxWeight, weight); // 选出服务列表最大的权重
			minWeight = Math.min(minWeight, weight); // 选出服务列表最小的权重
			if (weight > 0) {
				invokerToWeightMap.put(serviceInvoker, new IntegerWrapper(weight)); // 服务与权重值映射
				weightTotal += weight;
			}
		}

		String key = invokers.get(0).getKey();
		AtomicInteger sequence = sequences.get(key); // 调用累加次数
		if (sequence == null) {
			sequences.putIfAbsent(key, new AtomicInteger());
			sequence = sequences.get(key);
		}

		int currentSequence = sequence.getAndIncrement();

		/**
		 * 加权轮询
		 * <p>
		 * 这里类似于RandomLoadBalance. 判定 mod 落在哪个权重标识的范围内.
		 */
		if (maxWeight > 0 && minWeight < maxWeight) {
			int mod = currentSequence % weightTotal; // mod 范围 [ 0, weightTotal )
			for (int i = 0; i < maxWeight; i++) { // 这里取maxWeight为上界，因为循环到 maxWeight -1 时, 最大的权重也递减至0了
				for (Map.Entry<ServiceInvoker, IntegerWrapper> each : invokerToWeightMap.entrySet()) {
					final ServiceInvoker k = each.getKey();
					final IntegerWrapper v = each.getValue();
					// 如果大于0，则递减。直至当mode = 0 时，第一个 value > 0 的项 （边界）
					if (mod == 0 && v.getValue() > 0) {
						return k;
					}
					if (v.getValue() > 0) {
						v.decrement();
						mod--;
					}
				}
			}
		}
		/**
		 * 权重相同的情况下直接用取模 Round robin
		 */
		return invokers.get(currentSequence % length);
	}

	@Data
	@AllArgsConstructor
	private static final class IntegerWrapper {
		private int value;

		public void decrement() {
			this.value--;
		}
	}
}
