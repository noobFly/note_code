package com.noob.sort.impl;

import com.noob.sort.AbstractSort;

/**
 * 不稳定（因为相同的值最终有可能下标小的先置换到末尾的已排序队列中，可在比较父子节点大小的判断中增加若相等也置换，那应该能达到稳定排序的要求，
 * 但多了很多没必要的置换） 二叉堆 完全二叉树只是在最后一层要么满子节点，要么都是倒数第二层左边的节点开始满起来。
 * <p/>
 * Created by bear on 2016/3/3.
 */
public class HeapSort extends AbstractSort {

    /**
     * 无论是构建大根堆还是小根堆，循环的结束是指针指向了元数组的0下标位置。因为param[0]才是整个树的root。 2*root + 1 与
     * 2*root + 2 都没办法在没有新数组的情况下成为整个树的root。
     * 若在构建小根堆时，start--,虽然本父子节点组的顺序是对的。但是可能会导致父节点同级的兄弟节点间的值不按正序排列
     * 所以，有必要将最大值或者最小值置换到最后一个end_index 上。 没有叶子节点是完全可以的，根节点不变。
     *
     * @param param
     * @return
     */
    public int[] sort(int[] param) {
        sort_max_heap(param);
        return param;
    }

    /**
     * 从小到大排序 构建大根堆。获取最大数组下标在二叉树中的父节点。
     * 从这个父节点开始直至树根节点，比较是否符合父节点要大于等于任意子节点的值，否则将最大值替换置父节点上 最终能使得最大值一定是在树根节点。
     *
     * @param param
     */
    private void sort_max_heap(int[] param) {
        for (int i = 0; i < param.length; i++) {
            int end_index = param.length - 1 - i; //未排序队列最大下标
            if (end_index > 0) {
                createMaxHeap(param, end_index);
                // 每次循环后都是将当前未排序队列中最大值替换到最后。max_index 每次都少1
                swap(param, 0, end_index);
            }
        }
    }

    /**
     * @param param
     * @param lastIndex 最后一个值的下标
     */

    private void createMaxHeap(int[] param, int lastIndex) {
        for (int i = (lastIndex - 1) / 2; i >= 0; i--) {//获取根节点的下标
            int root = i;
            int bigger_index = 2 * root + 1;//左右子节点中值最大的下标
            if (bigger_index < lastIndex) {
                // 若左节点的下标小于最大的坐标，说明有右节点
                if (param[bigger_index] < param[bigger_index + 1]) {
                    bigger_index++;
                }
            }

            if (param[root] < param[bigger_index]) {
                swap(param, root, bigger_index);
            }
        }
    }

    /**
     * 从大到小排序。 构建最小堆，从最后一组父子节点开始向树root节点递推，将最小值置于每组父子节点的父节点上，这样最小值一定会在树root节点上
     *
     * @param param
     */
    private void sort_min_heap(int[] param) {
        for (int i = 0; i < param.length; i++) {
            int end_index = param.length - 1 - i; //未排序队列最大下标
            if (end_index > 0) {
                createMinHeap(param, 0, end_index);
                // 每次循环后都是将当前未排序队列中最小值替换到最后。end_index 每次都少1
                swap(param, 0, end_index);
            }
        }

    }

    /**
     * 不一定有子节点，单一定有根节点。 1、判断是否有右节点； 2、获取最小子节点下标；3、最小子节点值是否比根父节点值小，若小则交换
     *
     * @param param
     * @param start
     * @param end 每次的end
     */
    private void createMinHeap(int[] param, int start, int end) {
        for (int root = (end - 1) / 2; root >= start; root--) {
            int min_index = 2 * root + 1; // 左节点
            if (min_index < end) {
                // 判断是否有右节点
                if (param[min_index] > param[min_index + 1]) {
                    min_index++;
                }
            }

            if (param[min_index] < param[root]) {
                swap(param, min_index, root);
            }
        }
    }

    public static void main(String[] args) {
        int[] param = new int[] { 5, 7, 3, 9, 2, 6, 1 };
        new HeapSort().sort(param);
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
