package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 不稳定 （每次选择的都是第一次达到最大/最小的值） 选择排序。 选中最大/小值置于已经排序队列的队尾，在未排序队列中递推执行 Created by
 * bear on 2016/2/29.
 */
public class SelectionSort extends AbstractSort {

    @Override
    public int[] sort(int[] param) {
        for (int i = 0; i < param.length; i++) {
            int min_index = getMinIndex(param, i);
            swap(param, i, min_index);
        }
        return param;
    }

    /**
     * 在指定起始位置之后队列中查找最小值的下标
     *
     * @param param
     * @param start  边界起始位置
     * @return
     */
    private int getMinIndex(int[] param, int start) {
        int min_index = start;
        for (int j = start + 1; j < param.length; j++) {
            if (param[j] < param[min_index]) {
                min_index = j;
            }
        }
        return min_index;
    }

}
