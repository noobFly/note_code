package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 稳定 冒泡排序： 相邻的两个值比较，将最大/小的值逐步换至队尾/首 Created by bear on 2016/3/2.
 */
public class BubbleSort extends AbstractSort {

    @Override
    public int[] sort(int[] param) {
        int count = 0; //已经排序过的次数
        int max_count = param.length - 1;//最大的排序次数
        while (count <= max_count) {
            boolean is_swap = false; // 判定本轮是否swap了，没有则说明数组已经有序， break
            for (int i = 1; i <= max_count - count; i++) { // 控制好边界
                if (param[i - 1] > param[i]) {
                    swap(param, i, i - 1);
                    is_swap = true;
                }
            }
            count++;
            if (!is_swap) {
                break;
            }
        }
      //  System.out.println(String.format("总量：%s, 轮询次数： %s", param.length, count));
        return param;
    }

    public static void main(String[] args) {
        int[] param = new BubbleSort().sort(new int[] { 1, 2, 3, 5, 8, 6, 4, 9, 7 });
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
