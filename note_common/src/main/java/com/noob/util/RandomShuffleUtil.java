package com.noob.util;

import java.util.Arrays;
import java.util.Random;

/**
 * 随机打乱数组
 * <p>
 * 从数组的最后一个位置（假设下标是n）开始向前扫描，然后随机生成一个 [ 0 , n)  的随机数，假设该随机数是r1，将数组最后一个位置（下标n）与r1位置互换，
 * <p>
 * 然后开始扫面下一个数（下标为n-1），随机生成一个  [  0，n-1 ） 之间的随机数，假设该随机数是r2，然后将数组倒数第二个位置（下标为n-1）与r2位置互换
 * <p>
 * 继续扫面下一个数（下标为n-2），一直迭代下
 * <p>
 * 在这个迭代过程中，可以保证扫面点左边的数字都是尚未确定位置的，而右边的数字都是已经安排好位置的。
 */
public class RandomShuffleUtil {
    private static Random rand = new Random();

    public static <T> void swap(T[] a, int i, int j) {
        T temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    // Collections.shuffle() 也是如此处理
    public static <T> void shuffle(T[] arr) {
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randInd = rand.nextInt(i);
            swap(arr, randInd, i - 1);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Integer[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
            shuffle(arr);
            System.out.println(Arrays.toString(arr));
        }
    }
}
