package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 不稳定（因为每次的排序队列都是在元队列基础上按步长重新分组后的，导致相同值的数位置发生变化。）
 * 按步长分组并插入排序，递减步长至0
 * 希尔排序是基于插入排序的以下两点性质而提出改进方法的：
 * <p/>
 * 插入排序在对几乎已经排好序的数据操作时， 效率高， 即可以达到线性排序的效率
 * 但插入排序一般来说是低效的， 因为插入排序每次只能将数据移动一位
 * 希尔排序的基本思想是：先将整个待排序的记录序列分割成为若干子序列分别进行直接插入排序，待整个序列中的记录“基本有序”时，再对全体记录进行依次直接插入排序。
 * <p/>
 * 算法步骤：
 * <p/>
 * 1）选择一个增量序列t1，t2，…，tk，其中ti>tj，tk=1；
 * <p/>
 * 2）按增量序列个数k，对序列进行k 趟排序；
 * <p/>
 * 3）每趟排序，根据对应的增量ti，将待排序列分割成若干长度为m 的子序列，分别对各子表进行直接插入排序。仅增量因子为1 时，整个序列作为一个表来处理，表长度即为整个序列的长度。
 * Created by bear on 2016/2/29.
 */
public class ShellSort extends AbstractSort {

    public int[] sort(int[] param) {
        int length = param.length;
        for (int gap = length / 2; gap > 0; gap /= 2) {//步长递减至1,核心思想与插入排序一致
        	
            for (int i = 0; i <= gap; i++) {//按步长分组[0, gap, 2gap, 3gap],[1, 1 + gap, 1 + 2gap, 3 + 3gap]...
            	
                for (int m = i; m < length - gap; m += gap) {//在每一个分组中进行排序,从第二个值m开始向前递推比较。比较完后m++
                	
                    for (int j = m + gap; j >= i + gap; j -= gap) {//快排
                        if (param[j - gap] > param[j]) {
                            swap(param, j, j - gap);

                        }
                    }
                }

            }
        }

        return param;
    }


}
