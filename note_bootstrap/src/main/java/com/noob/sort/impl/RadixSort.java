package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 按个位、十位、百位...排序 -----> 当前版本对负值无效
 * <p/>
 * 稳定排序 基数排序 Created by bear on 2016/3/6.
 */
public class RadixSort extends AbstractSort {
	private int[] radix = new int[] { 1, 1, 10, 100, 1000 }; // 与数组的最大值的量级有关
	private int max_index = 10; // 桶的数量 因为阿拉伯数是0-9
	private int max_position = 3; // 数组最大值的位数

	public int[] sort(int[] param) {
		for (int position = 0; position < max_position; position++) {
			sort_core(param, position);
		}
		return param;
	}

	/**
	 * 按指定位上的数值排序
	 *
	 * @param param
	 * @param end_index
	 * @param position
	 */
	private void sort_core(int[] param, int position) {
		int cap = param.length - 1;

		int[] count = new int[max_index];// 记录每个桶统计个数
		int[] store = new int[cap + 1]; // 桶的容量

		// 置空
		for (int i = 0; i < max_index; i++) {
			count[i] = 0;
		}

		// 指定位的数值与桶的编号index一一对应。统计对应的数值下有多少个
		for (int i = 0; i <= cap; i++) {
			count[getDigit(param[i], position)]++;
		}
		
		// 通过累加可以确定每个指定位的值在store桶上位置索引的上界。
		for (int i = 1; i < max_index; i++) {
			count[i] = count[i] + count[i - 1];
		}
		// 从后向前, 保证稳定性。 因为插入count[]时，越后的数字 索引值越大
		for (int i = cap; i >= 0; i--) {
			int digit = getDigit(param[i], position);
			store[count[digit] - 1] = param[i]; // 放入对应的桶中
			count[digit]--; // 对应桶索引的装入数据量减一
		}

		// 将已分配好的桶中数据再倒出来，此时已是对应当前位数有序的表
		for (int i = 0, j = 0; i <= cap; i++, j++) {
			param[i] = store[j];
		}
	}

	/**
	 * 获取指定位上的数值
	 */
	private int getDigit(int param, int digit) {
		return (param / radix[digit]) % 10;
	}

}
