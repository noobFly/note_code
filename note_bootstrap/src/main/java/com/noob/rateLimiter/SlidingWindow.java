package com.noob.rateLimiter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 滑动窗口-- 不严谨的代码demo
 * <p>
 *
 */
@Slf4j
public class SlidingWindow {

	/* 假定为环状队列 */
	private AtomicReference<SlidingCounter>[] timeSlices;
	/* 队列的总长度 */
	private int timeSliceTotalSize;
	/* 每个时间片的时长 毫秒 */
	private int timeSlice;
	/* 统计窗口 时间片个数 */
	private int windowSize;

	public SlidingWindow(int timeSlice, int windowSize) {
		this.timeSlice = timeSlice;
		this.windowSize = windowSize;
		this.timeSliceTotalSize = windowSize * 100 + 1; // 这里最好设置较大些。在reset单个timeSlices[index]时，可以降低误差
		initTimeSlices();
	}

	/**
	 * 初始化队列
	 */
	@SuppressWarnings("unchecked")
	private void initTimeSlices() {
		if (timeSlices != null) {
			return;
		}
		timeSlices = new AtomicReference[timeSliceTotalSize];
		for (int i = 0; i < timeSliceTotalSize; i++) {
			timeSlices[i] = new AtomicReference<SlidingCounter>(new SlidingCounter());
		}
	}

	// 定位当前时间在哪个时间片上
	private SlidingCounter locationIndex() {
		long time = System.currentTimeMillis();
		int index = (int) ((time / timeSlice) % timeSliceTotalSize);

		return new SlidingCounter(time - time % timeSlice, index);
	}

	/**
	 * 对时间片计数+1，并返回窗口中所有的计数总和
	 * <p>
	 * 使用 synchronized 应该更能保证原子性
	 */
	public boolean allow(int threshold) {
		SlidingCounter newSlidingCounter = locationIndex();
		int index = newSlidingCounter.getIndex();
		long time = newSlidingCounter.getOffsetTime();

		int sum = 0;
		// cursor等于index，返回true，否则返回false，并会将cursor设置为index
		SlidingCounter oldSlidingCounter = timeSlices[index].get();
		// 更新时间片上的新周期信息
		if (oldSlidingCounter.getIndex() != index || oldSlidingCounter.getOffsetTime() != time) {
			// compareAndSet为防止并发修改且offsetTime、index 需要原子性统一更新，所以就不能直接在原对象上变更。
			boolean reset = timeSlices[index].compareAndSet(oldSlidingCounter, newSlidingCounter);// 这里可能是更新不上的，所以整个过程需要重写为for循环处理，并设置好超时时间或重试次数来break线程。
			log.info("变动计数器 idnex:{}, offset:{}, 结果:{}", index, time, reset);

		}

		for (int i = 0; i < windowSize; i++) {
			sum += timeSlices[(index - i + timeSliceTotalSize) % timeSliceTotalSize].get().getValue().get(); // 兼容处理环状队列的首尾情况
		}

		// 阈值判断 sum是从0开始
		if (sum < threshold) { // 这个判定+increment 是非原子性, 并发下会出问题。
			// 未超过阈值才+1
			timeSlices[index].updateAndGet(t -> {
				t.getValue().incrementAndGet();
				return t;
			});
			return true;
		}

		return false;

	}

	/**
	 * 计数器
	 *
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	static class SlidingCounter {
		private long offsetTime = 0; // 时间分片的唯一值
		private int index = 0; // 滑动队列数组下标
		private AtomicInteger value = new AtomicInteger(0);

		public SlidingCounter(long offsetTime, int index) {
			this.offsetTime = offsetTime;
			this.index = index;
		}

	}

	public static void main(String[] args) throws InterruptedException {
		SlidingWindow slidingWindow = new SlidingWindow(500, 1);
		for (int i = 0; i < 5; i++) {
			new Thread(() -> {
				int loop = 0;
				while (loop < 30) {
					log.info("{}", slidingWindow.allow(2));
					loop++;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		Thread.sleep(100000);
	}
}
