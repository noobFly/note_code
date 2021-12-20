package com.noob.sort.leetcode;

import java.util.ArrayDeque;

/**
 * 
 * 接雨水
 * <p>
 * 这题的关键是要确认： 地势低洼下才能蓄水，取决于当前位置左右两边最大块高两者最小值。
 * <p>
 * 解法一： 按最大块高level 从底部0开始遍历到层高level， 累加当前层高每个下标可以蓄水值
 * <p>
 * 解法二： 动态规划先确定每一个下标它左右两边最大块高, 那最大可蓄水量是 ： 两者最小值 - 当前块高
 * <p>
 * 解法三： 利用 “斜向上”的思维： 迭代 找到当前下标后面第一个比它高的位置，累加蓄水量, 下一次迭代以这个高位置块为基准 ；
 * 直到当前下标是最高的，再从后向前相同方式处理
 * <p>
 * 解法四： 单调递减栈，栈元素是下标，栈顶的块高是栈里下标的块高中最的小。 当遍历到一个新的下标时，块高不大于栈顶则入栈，
 * 大于时则表示栈顶下标处是可以蓄水的 ， 当前下标是右挡板，那栈顶元素的前一个栈元素则是左挡板，计算栈顶元素下标处的蓄水量， 这里的逻辑是：
 * 从该左挡板位置到当前下标(右挡板)位置的水位高度是min(左右挡板高度) 。 然后再依次和下一个栈元素比较,
 * 大于时累加计算水量，直到不大于栈元素则再次入栈。
 * <p>
 * 解法四通过栈的方式记录了历史递减的块高， 类似于“斜向下”的方式来处理，利用的是level层高从底部到高处的处理方式。
 * 每一个从后向前（栈顶到栈尾）计算都是将水位抬高到min处， 那在计算前一个栈元素到当前index的可加水位时是
 * <p>
 * (min(height_cur, height_pre) - height_top) * (cur - pre - 1)
 */
public class Solution42 {

	/**
	 * 先确定最大高度，从底层向上逐层遍历。
	 * <p>
	 * 每一层判定，当前index与层高的差额表示它可能可以蓄积水的体积，取决于后面是否有大于等于当前层高的index_right。
	 * <p>
	 * 要注意从0开始连续不可蓄情况
	 */
	public static int trap1(int[] height) {
		int sum = 0;
		int max = getMax(height);
		for (int level = 1; level <= max; level++) { // 最大遍历高度
			boolean hasLeftBlock = false; // 标记是否有左界， 初始false也是为了屏蔽开始坐标高度为0的情况
			int temp_sum = 0;
			for (int j = 0; j < height.length; j++) {
				if (hasLeftBlock && height[j] < level) {
					temp_sum++; // 当前坐标高度小于遍历高度, 临时temp + 1 来表示可以蓄水到当前遍历高度的体积
				}

				// 判定当前level高的右界。
				// 当有坐标高度大于等于遍历高度, 则说明之前小于遍历高度的坐标都可以蓄满到当前遍历高度
				if (height[j] >= level) {
					sum = sum + temp_sum;
					temp_sum = 0;
					hasLeftBlock = true; // 有左界
				}
			}
		}
		return sum;
	}

	/**
	 * 水量取决于木桶最小的那块板，
	 * <p>
	 * 先从前向后遍历， “斜向上”可蓄水, 当前current_index是当前短板。
	 * <p>
	 * 直到最高值的max_index, 再从后向前遍历
	 * <p>
	 * “斜向下” 当计算了某个块的水位后，如果后一个块高又更大，前面块水位可能需要重新计算; 它的
	 * <p>
	 * 而“斜向上” 没有这个问题，更容易控制。
	 */
	public static int trap2(int[] height) {
		int total = 0; // 结果
		/**
		 * step1: 先确定全量的蓄水范围
		 */
		int start = 0;
		int end = height.length - 1;
		// 找到第一个不为0的起始边界
		while (start < height.length && height[start] == 0) {
			start++;
		}
		if (start > height.length - 1) {
			return total;
		}
		// 找到最后一个不为0的截止边界
		while (end >= 0 && height[end] == 0) {
			end--;
		}
		if (end < 0 || end - start < 2) {
			return total;
		}

		/**
		 * step2. 在[start, end] 范围内 首先从前向后遍历,
		 * <p>
		 * 判定每一次的边界和蓄水体积。 这里的条件是: [右边的高度要大于左边 或 右边高度等于左边且index相差] 相当于 "斜向上"
		 * <p>
		 * 
		 */

		int left_max = start; // 从左向后递推到最高的位置

		// 从左向右， 确定最大的index
		while (start < height.length) {
			int block = 0; // 存在的方块
			boolean hashNextUp = false; // 后续是否有大于等于的块高,

			for (int right = start + 1; right <= end; right++) {
				if (height[right] < height[start] || (height[right] == height[start] && (right - start) == 1)) {
					// 右边小，暂定不可蓄水
					block += height[right];
					continue;
				} else {
					hashNextUp = true;
					// 左边 < 右边 || (左边 == 右边 && 有间隔) 这样才能蓄水
					total += (right - start - 1) * height[start] - block; // 长 * 宽 - block
					start = right; // 下一轮的边界
					break;
				}
			}

			if (!hashNextUp) { // 后续没有任何比height[left] 更高或相等的了。
				left_max = start;
				break;
			}

		}

		/**
		 * step3: 从右向左截止到left_max 计算水位体积。
		 * <p>
		 * 因为left_max 已经是数组内最大值了， 所以从右向左递推全局上也是 相当于 "斜向上"(矮的向高的靠齐)
		 */
		int start_end = end;
		while (start_end > left_max) {
			int block_end = 0;
			boolean hashNextUp = false; // 后续是否有大于等于的块高,

			for (int left = start_end - 1; left >= left_max; left--) {
				if (height[left] < height[start_end]
						|| (height[left] == height[start_end] && (start_end - left) == 1)) {
					// 不可蓄水
					block_end += height[left];
					continue;
				} else {
					hashNextUp = true;
					// 右边 < 左边 || (左边 == 右边 && 有间隔) 这样才能蓄水
					total += (start_end - left - 1) * height[start_end] - block_end; // 长 * 宽 - block
					start_end = left; // 下一轮的边界
					break;
				}
			}
			if (!hashNextUp) {
				break; // 一定要有跳出条件， eg. [4,2,3,5,1,9,9,6] 这种情况下对于两个连续9需要break
			}

		}

		return total;
	}

	/**
	 * trap2方法是先 从左向右直到最大的数值位置，再从右向左遍历。 它的问题是： 重复遍历了(max, end] 位置数据。
	 * <p>
	 * 优化方案： 前后双指针, 计算完当前水量后, 移动小的那一个。 原理也是保证"斜向上"。
	 * 
	 */
	public static int trap3(int[] height) {
		int total = 0; // 结果
		/**
		 * step1: 先确定全量的蓄水范围
		 */
		int start = 0;
		int end = height.length - 1;
		// 找到第一个不为0的起始边界
		while (start < height.length && height[start] == 0) {
			start++;
		}
		if (start > height.length - 1) {
			return total;
		}
		// 找到最后一个不为0的截止边界
		while (end >= 0 && height[end] == 0) {
			end--;
		}
		if (end < 0 || end - start < 2) {
			return total;
		}

		/**
		 * step2. 在[start, end] 范围内双向遍历
		 * <p>
		 * 前后双指针向中间递推，每次先移动的是值小的那个指针， 保证 低的向高靠齐 整体上的“斜向上”
		 */
		int left_max = 0, right_max = 0; // 高度
		while (start < end) {
			if (height[start] < height[end]) { // 这里先移动的是高度小的指针 , 这样另外一边一定有大于该指针的高度块可以挡水
				if (height[start] >= left_max) {
					left_max = height[start]; // 更新成： 更高的高度值
				} else {
					total += left_max - height[start]; // 小于left_max标识这里可以蓄水，因为end指针一定比start大，所以这里至少水位是left_max
				}

				start++;

			} else {
				if (height[end] >= right_max) {
					right_max = height[end];
				} else {
					total += right_max - height[end]; // 小于right_max, 表示这里可蓄水，至少水位是right_max
				}
				end--;
			}

		}

		return total;
	}

	/**
	 * 遍历每一个坐标，它能存蓄的水量取决于 左边最高 与 右边最高 的最小值
	 * <p>
	 * 左边最大值： max_left[i] = max_left[i - 1] 与 height[i] 的最大值
	 * <p>
	 * 右边最大值： max_right[i] = max_right[i + 1] 与 height[i] 的 最大值
	 */
	public static int trap4(int[] height) {
		int total = 0; // 结果
		/**
		 * step1: 先确定全量的蓄水范围
		 */
		int start = 0;
		int end = height.length - 1;
		// 找到第一个不为0的起始边界
		while (start < height.length && height[start] == 0) {
			start++;
		}
		if (start > height.length - 1) {
			return total;
		}
		// 找到最后一个不为0的截止边界
		while (end >= 0 && height[end] == 0) {
			end--;
		}
		if (end < 0 || end - start < 2) {
			return total;
		}
		int[] max_left_array = new int[height.length]; // 每个坐标下的左边大值
		int[] max_right_array = new int[height.length]; // 每个坐标下的最右边大值

		/**
		 * 动态规划的核心是： 复用前面产生的计算结果 step2 : 从后向前，确定每一个坐标 它右边的最大值。
		 */
		max_right_array[end] = height[end]; // 最后一个最大值等于它本身高度
		// 先填充max_right_array 从后向前
		for (int i = end - 1; i >= start; i--) {
			max_right_array[i] = Math.max(max_right_array[i + 1], height[i]);
		}

		/**
		 * step3 : 从前向后，确定每一个坐标 它左边的最大值。 并取左右最大值的MIN来判定蓄水量
		 */
		max_left_array[start] = height[start]; // 首位的最大值等于本身
		for (int i = start + 1; i < end; i++) {
			// 找左边最大
			max_left_array[i] = Math.max(max_left_array[i - 1], height[i]);
			int min = Math.min(max_left_array[i], max_right_array[i]); // 取最短的那块板
			if (min > height[i]) {
				total += min - height[i]; // 只有大于自己的高度才能蓄水
			}
		}

		return total;
	}

	public static int trap5(int[] height) {
		int total = 0; // 结果
		/**
		 * step1: 先确定全量的蓄水范围
		 */
		int start = 0;
		int end = height.length - 1;
		// 找到第一个不为0的起始边界
		while (start < height.length && height[start] == 0) {
			start++;
		}
		if (start > height.length - 1) {
			return total;
		}
		// 找到最后一个不为0的截止边界
		while (end >= 0 && height[end] == 0) {
			end--;
		}
		if (end < 0 || end - start < 2) {
			return total;
		}

		// 栈是单调递减, 栈顶元素的高度最小，栈底最大。
		// 这个也是可以处理“斜向下”
		ArrayDeque<Integer> queue = new ArrayDeque<Integer>(height.length);

		for (int i = start; i <= end; i++) {
			while (!queue.isEmpty() && height[i] > height[queue.getFirst()]) { // 栈不为空 且 当前块比栈顶的高
				// 计算
				int first = queue.pollFirst();
				if (queue.isEmpty()) {
					break; // 栈为空说明左边再没有挡板了, 是无法蓄水的, 直接压入当前index
				}
				// 通过画图可知，这里计算区间体积是按高度层级来算。
				// 因为这个栈是单调递减, 栈顶元素的高度最小，栈底最大。
				total += (Math.min(height[i], height[queue.getFirst()]) - height[first]) * (i - queue.getFirst() - 1); // （取短边的高度
																														// -
																														// 栈顶）
																														// *
																														// 长
			}

			queue.addFirst(i);
		}

		return total;
	}

	private static int getMax(int[] height) {
		int max = 0;
		for (int i = 0; i < height.length; i++) {
			if (height[i] > max) {
				max = height[i];
			}
		}
		return max;
	}

	public static void main(String[] args) {

		System.out.println(trap5(new int[] { 0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1 }));
	}

}
