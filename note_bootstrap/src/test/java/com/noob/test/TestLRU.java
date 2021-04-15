package com.noob.test;

import java.util.Random;

import com.noob.LRU.LRUCache;

public class TestLRU {
	/**
	 * 新增或移动是在末尾，删除是在首位
	 */
	public static void main(String[] args) {
		LRUCache<Integer, Integer> cache = new LRUCache<Integer, Integer>(5);
		Random randow = new Random();

		for (int i = 0; i < 100; i++) {
			int nextInt = randow.nextInt(10);
			cache.put(nextInt, i);
			System.out.println(String.format("add：%s, view: %s", nextInt, cache.linkNodeView()));
		}
	}
}

/*
add：1, view: 1
add：3, view: 1,3
add：1, view: 3,1
add：7, view: 3,1,7
add：9, view: 3,1,7,9
add：5, view: 3,1,7,9,5
add：1, view: 3,7,9,5,1
add：2, view: 7,9,5,1,2
add：3, view: 9,5,1,2,3
add：6, view: 5,1,2,3,6
add：2, view: 5,1,3,6,2
add：9, view: 1,3,6,2,9
add：6, view: 1,3,2,9,6
add：8, view: 3,2,9,6,8
add：5, view: 2,9,6,8,5
add：9, view: 2,6,8,5,9
add：3, view: 6,8,5,9,3
add：1, view: 8,5,9,3,1
add：4, view: 5,9,3,1,4
add：5, view: 9,3,1,4,5
add：1, view: 9,3,4,5,1
add：3, view: 9,4,5,1,3
add：2, view: 4,5,1,3,2
add：4, view: 5,1,3,2,4
add：3, view: 5,1,2,4,3
add：6, view: 1,2,4,3,6
add：1, view: 2,4,3,6,1
add：3, view: 2,4,6,1,3
add：2, view: 4,6,1,3,2
add：5, view: 6,1,3,2,5
add：1, view: 6,3,2,5,1
add：7, view: 3,2,5,1,7
add：9, view: 2,5,1,7,9
add：1, view: 2,5,7,9,1
add：4, view: 5,7,9,1,4
add：4, view: 5,7,9,1,4
add：9, view: 5,7,1,4,9
add：6, view: 7,1,4,9,6
add：3, view: 1,4,9,6,3
add：4, view: 1,9,6,3,4
add：7, view: 9,6,3,4,7
add：1, view: 6,3,4,7,1
add：5, view: 3,4,7,1,5
add：1, view: 3,4,7,5,1
add：2, view: 4,7,5,1,2
add：3, view: 7,5,1,2,3
add：5, view: 7,1,2,3,5
add：6, view: 1,2,3,5,6
add：2, view: 1,3,5,6,2
add：2, view: 1,3,5,6,2
add：2, view: 1,3,5,6,2
add：5, view: 1,3,6,2,5
add：6, view: 1,3,2,5,6
add：7, view: 3,2,5,6,7
add：8, view: 2,5,6,7,8
add：8, view: 2,5,6,7,8
add：9, view: 5,6,7,8,9
add：0, view: 6,7,8,9,0
add：2, view: 7,8,9,0,2
add：2, view: 7,8,9,0,2
add：2, view: 7,8,9,0,2
add：1, view: 8,9,0,2,1
add：6, view: 9,0,2,1,6
add：0, view: 9,2,1,6,0
add：8, view: 2,1,6,0,8
add：9, view: 1,6,0,8,9
add：3, view: 6,0,8,9,3
add：4, view: 0,8,9,3,4
add：6, view: 8,9,3,4,6
add：2, view: 9,3,4,6,2
add：5, view: 3,4,6,2,5
add：2, view: 3,4,6,5,2
add：9, view: 4,6,5,2,9
add：8, view: 6,5,2,9,8
add：2, view: 6,5,9,8,2
add：4, view: 5,9,8,2,4
add：0, view: 9,8,2,4,0
add：3, view: 8,2,4,0,3
add：0, view: 8,2,4,3,0
add：3, view: 8,2,4,0,3
add：9, view: 2,4,0,3,9
add：7, view: 4,0,3,9,7
add：9, view: 4,0,3,7,9
add：9, view: 4,0,3,7,9
add：0, view: 4,3,7,9,0
add：1, view: 3,7,9,0,1
add：3, view: 7,9,0,1,3
add：8, view: 9,0,1,3,8
add：8, view: 9,0,1,3,8
add：9, view: 0,1,3,8,9
add：7, view: 1,3,8,9,7
add：4, view: 3,8,9,7,4
add：0, view: 8,9,7,4,0
add：1, view: 9,7,4,0,1
add：6, view: 7,4,0,1,6
add：6, view: 7,4,0,1,6
add：9, view: 4,0,1,6,9
add：1, view: 4,0,6,9,1
add：8, view: 0,6,9,1,8
add：2, view: 6,9,1,8,2
*/
