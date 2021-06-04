package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 不稳定（判断时，若相等，则出现不稳定状况） 快速排序：关键值排序
 * （1）基本思想：选择一个基准元素,通常选择第一个元素或者最后一个元素,通过一趟扫描，将待排序列分成两部分,一部分比基准元素小,一部分大于等于基准元素,
 * 此时基准元素在其排好序后的正确位置,然后再用同样的方法递归地排序划分的两部分。
 * <p>
 * Created by bear on 2016/3/2.
 */
public class QuickSort extends AbstractSort {
	@Override
	public int[] sort(int[] param) {
		return sort_core(param, 0, param.length - 1);
	}

	/**
	 * 左边 < 关键值 <= 右边
	 *
	 * @param param
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return
	 */

	private int[] sort_core(int[] param, int start, int end) {
		int radix = param[start]; // 选开始位置为基准数
		int low = start + 1; // 队首指针开始位置为除基准数后下标
		int high = end; // 队尾指针开始位置

		while (low < high) {// 确保两个指针不交叉！！ 快排思想：后面部分是大于等于基准数的，所以 队尾指针的数值相等也左移,队首则不移动
			// 从后向前找到比radix要小的
			while (param[high] >= radix && low < high) {
				// 队尾指针的值 >= 基准值 & 队首指针下标 < 队尾首指针下标
				high--; // 队尾指针前移
			}
			// 从前向后找到比radix要大的
			while (param[low] < radix && low < high) {
				// 队首指针的值 < 基准值 & 队首指针下标 < 队尾首指针下标
				low++; // 队首指针后移
			}
			// 这里最极限也就是 low == high
			if (low != high) {
				swap(param, low, high); // 换好之后 param[high] 一定 > param[low]
			}

		}
		if (low == high) {
			int same = low = high;
			// 基准数大, 则边界就是这个low; 如果基准数小, 则边界是low-1. 并swap.
			Integer limit = radix > param[same] ? same : same - 1; //要考虑same根本就没移动过的情况
			swap(param, start, limit);
			// 分割成左右2部分
			if (limit - 1 > start) {
				sort_core(param, start, limit - 1); // 左部分
			}
			if (limit + 1 < end) {
				sort_core(param, limit + 1, end); // 右部分
			}

		} else {
			System.out.println(String.format("error test: low %s != high %s ", low, high));

		}
		// System.out.println(toString(param));
		return param;

	}

	public static void main(String[] args) {
		int[] param = new int[] { 5, 4, 3, 2, 1, 10, 15, 4, 0, 2, 5, 6, 4, 9, 5 };
		System.out.println(toString(new QuickSort().sort(param)));

	}

}
