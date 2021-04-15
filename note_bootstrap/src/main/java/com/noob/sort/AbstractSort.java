package com.noob.sort;

/**
 * 关于时间复杂度：
 * <p/>
 * (1)平方阶(O(n2))排序 各类简单排序:直接插入、直接选择和冒泡排序；
 * <p/>
 * (2)线性对数阶(O(nlog2n))排序 　　快速排序、堆排序和归并排序； (3)O(n1+§))排序,§是介于0和1之间的常数。
 * <p/>
 * 希尔排序 (4)线性阶(O(n))排序 基数排序，此外还有桶、箱排序。
 * <p/>
 * <p/>
 * <p/>
 * 关于稳定性：
 * <p/>
 * 稳定的排序算法：冒泡排序、插入排序、归并排序和基数排序
 * <p/>
 * 不稳定的排序算法：选择排序、快速排序、希尔排序、堆排序 Created by bear on 2016/2/29.
 */
public abstract class AbstractSort implements Sort {

    public abstract int[] sort(int[] param);

    public void print() {
        int[] param = new int[] { 17, 5, 4, 32,  0, 44, 22, 12, 32, 54, 22, 88, 77, 99, 11, 9, 14, 6, 5,  9, 4, 2,
		        5, 6, 4, 9 };
		StringBuilder str = toString(
                sort(param)).append("-----").append(this.getClass().getName());
        System.out.println(str.toString());
    }

    public void swap(int[] param, int source, int target) {
        if (source != target) {
            int temp = param[source];
            param[source] = param[target];
            param[target] = temp;
        }
    }

    public static StringBuilder toString(int[] param) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < param.length; i++) {
            str.append(param[i]);
            if (i != param.length - 1) {
                str.append(" ");
            }
        }
        return str;
    }

}
