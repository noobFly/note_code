package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 稳定
 * 归并排序： 采用分治法，递归将数组分割成两部分成两个已排序队列，再合并至新的数组中
 * Created by bear on 2016/3/2.
 */
public class MergeSort extends AbstractSort {

	// 二分数组直到只剩单个元素
    @Override
    public int[] sort(int[] param) {
        int length = param.length;
        if (length > 1) {
            int split = length / 2;
            int[] low = splitArray(param, 0, split - 1); 
            int high[] = splitArray(param, split, param.length - 1);
            //切割数组后排序
            param = sort_core(sort(low), sort(high));
        }
        return param;
    }

    /**
     * 两个已从小至大排序的数组合并
     *
     * @param low
     * @param hight
     * @return
     */
    private int[] sort_core(int[] low, int[] hight) {
        int low_length = low.length;
        int high_length = hight.length;
        int[] result = new int[low_length + high_length];
        int i = 0, j = 0, m = 0;

        while (i < low_length || j < high_length) {//只有当两个数组都取完才退出循环
            if (i < low_length && (j >= high_length || low[i] < hight[j])) {
                //当low还没被取完时，如果high已经被取完或者low_value < hight_value时，将low_value置于新数组中，下标+1
                result[m++] = low[i];
                i++;
            } else {
                result[m++] = hight[j];
                j++;
            }
        }
        return result;
    }

    /**
     * 切分数组
     *
     * @param param
     * @param start 开始下标
     * @param end   结束下标
     * @return
     */
    private int[] splitArray(int[] param, int start, int end) {
        int[] result = new int[end - start + 1];
        for (int i = start, m = 0; i <= end; i++, m++) {
            result[m] = param[i];
        }
        return result;
    }
}
