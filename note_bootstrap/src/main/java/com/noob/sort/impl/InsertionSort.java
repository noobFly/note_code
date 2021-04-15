package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 稳定 插入排序 1）将待排序序列第一个元素看做一个有序序列，把第二个元素到最后一个元素当成是未排序序列。
 * <p/>
 * 2）从头到尾依次扫描未排序序列，将扫描到的每个元素插入有序序列的适当位置。（如果待插入的元素与有序序列中的某个元素相等，则将待插入元素插入到相等元素的后面
 * 。） Created by bear on 2016/2/29.
 */
public class InsertionSort extends AbstractSort {

    /**
     * 假定前面都是已从小至大排序的队列：如果当前下标的值小于前一下标的值，交换位置，并在已排序队列中从后向前递推再判断；
     *
     * @param param
     * @return
     */
    public int[] sort(int[] param) {
        for (int i = 1; i < param.length; i++) {
            for (int j = i - 1; j >= 0; j--) {
                if (param[j] > param[j + 1]) {
                    swap(param, j, j + 1);
                } else {
                    break; // 如果后面的要大，则break;
                }
            }

        }

        return param;
    }

    public static void main(String[] args) {
        int[] param = new InsertionSort().sort(new int[] { 4, 5, 3, 5, 8, 6, 4, 9, 7 });
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < param.length; i++) {
            str.append(param[i]);
            if (i != param.length - 1) {
                str.append(" ");
            }
        }
        System.out.println(str.toString());

    }
}
