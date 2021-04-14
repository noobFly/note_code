package com.noob.loadBalance.impl;

import java.util.Random;
import java.util.TreeMap;

/**
 * 
 * 大致思路是：
 * <p>
 * 在横轴上以权重值为定位点，[ 0, 权重累加值 ) 为整个权重范围。 请求hash值落点入横轴。
 * <p>
 * [0+weight1+...+weight(N-1), 0+weight1+...+weight(N-1)+weightN ) 标识 weightN被命中
 * （判定请求的随机数 落在哪个最小权重点前）
 * <p>
 * 与 Random类中的nextInt(n)系列方法生成 [ 0, n) 的伪随机数 的范围有关系
 * <p>
 * eg.推论
 * <p>
 * weight - > 3, 5, 2 =》 权重点是： 3, 8 , 10 
 * <p>
 * 用 Math.nextInt(10) 生成随机整数范围是 [ 0, 10)
 * <p>
 * 则分别对应的 {0,1,2}, {3,4,5,6,7},{8,9} 权重范围是 [0, 3) , [3,8), [8,10) 左闭右开
 * <p>
 * TreeMap通过KEY进行排序, KEY需要实现{@link Comparable}接口。所以不能为空
 *
 */
public class RandomLoadBalance {
	static int loop_max = 1000;
	static double[] weightDouble = new double[] { 0.06, 2.94, 97 };
	static int[] weightInt = new int[] { 1, 1, 3 };

	
	public static void main(String[] args) {
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		map.put(1, 1);
		map.put(3, 3);
		map.put(5, 5);
		map.put(9, 9);
		map.put(7, 7);
		System.out.println(map.ceilingKey(4)); // 5
		System.out.println(map.ceilingKey(5)); // 5
		System.out.println(map.ceilingKey(10)); // null
		System.out.println(map.tailMap(1, false).firstKey()); // 3
		System.out.println(map.tailMap(1, true).firstKey()); // 1
		System.out.println(map.tailMap(10, false).firstKey()); // 超过范围:  java.util.NoSuchElementException tailMap返回空
		System.out.println(map.tailMap(10, true).firstKey()); //  超过范围 : java.util.NoSuchElementException tailMap返回空

	} 

	
	
	public static void sortByTreeMap() {

		int choose1 = 0, choose2 = 0, choose3 = 0;
		TreeMap<Double, Integer> map = new TreeMap<Double, Integer>();
		double weightTotal = 0;
		for (int index = 0; index < weightDouble.length; index++) {
			weightTotal += weightDouble[index];
			map.put(weightTotal, index + 1);
		}
		int loop = loop_max;

		while (loop > 0) {
			double random = (Math.random() * weightTotal); // 范围 [ 0, weightTotal )

			/**
			 * TreeMap.ceilingKey 返回最小的 大于或等于 入参key的key值, 如果没有返回null。
			 * <p>
			 * 但Math.random() [0, 1) 是 左开右闭， 所以不应该返回相等的key。
			 **/
			Integer index =  map.tailMap(random, false).firstEntry().getValue(); // 不包含匹配相等的key
			if (1 == index) {
				choose1++;
			} else if (2 == index) {
				choose2++;
			} else if (3 == index) {
				choose3++;
			}
			--loop;
			System.out.println(String.format("index1-%s index2-%s index3-%s", choose1, choose2, choose3));
		}


	}

	/**
	 * 1）假设有四个集群节点A,B,C,D,对应的权重分别是1,2,3,4,
	 * <p>
	 * 那么请求到A节点的概率就为1/(1+2+3+4) = 10%. B,C,D节点依次类推为20%,30%,40%.
	 * <p>
	 * 2）总权重为10(1+2+3+4), 根据10随机出一个整数,假如为随机出来的是2.然后依次和权重相减,比如2(随机数)-1(A的权重) = 1,
	 * <p>
	 * 然后1(上一步计算的结果)-2(B的权重) = -1,此时-1 < 0,那么则调用B,其他的以此类推
	 * 
	 */
	public static void sortInt() {

		int totalWeight = 0;
		boolean isAllSameWeight = true; // 判定权重值是否都是一样的
		int length = weightInt.length;
		for (int i = 0; i < length; i++) {
			int weight = weightInt[i];
			totalWeight += weight;
			if (isAllSameWeight && i > 0 && weight != weightInt[i - 1]) {
				isAllSameWeight = false;
			}
		}
		Random random = new Random();

		int choose1 = 0, choose2 = 0, choose3 = 0;
		int turn = loop_max;

		while (turn > 0) {
			int index = -1;
			if (totalWeight > 0 && !isAllSameWeight) {
				int offset = random.nextInt(totalWeight);
				for (int i = 0; i < length; i++) {
					offset -= weightInt[i];
					if (offset < 0) { // Random类中的nextInt(n)系列方法生成 [ 0, n ) 的伪随机正整数; 所以 offset == 0 时, 应该再递推后一个index
										// (见类注释推论)
						index = i;
						break;
					}
				}
			}
			// 权重值都一样则随机任何一个数值
			if (index == -1) {
				index = random.nextInt(length);
			}
			if (0 == index) {
				choose1++;
			} else if (1 == index) {
				choose2++;
			} else if (2 == index) {
				choose3++;
			}
			--turn;
		}
		System.out.println(String.format("index1-%s index2-%s index3-%s", choose1, choose2, choose3));

	}
}