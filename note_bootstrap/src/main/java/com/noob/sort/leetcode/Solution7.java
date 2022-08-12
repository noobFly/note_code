package com.noob.sort.leetcode;

public class Solution7 {
    public static int reverse(int x) {
        if (-9 < x && x < 9) return x; // 个位数 不用处理
        int result = 0;
        while (x != 0) {
            if (result > Integer.MAX_VALUE / 10 || result < Integer.MIN_VALUE / 10) { // 因为限定是int,所以要判定转换后的数值溢出
                return 0;
            } else {
                result = result * 10 + x % 10; // 负数时取模也是负数
                x /= 10;
            }
        }
        return result;
    }

    public static void main(String args[]) {
        System.out.println(reverse(1534236469));
    }
}