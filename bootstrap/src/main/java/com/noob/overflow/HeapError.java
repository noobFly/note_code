package com.noob.overflow;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

/**
 * 堆溢出
 * <p>
 * <p>
 * 内存泄漏是指对象实例在新建和使用完毕后，仍然被引用，没能被垃圾回收释放，一直积累，直到没有剩余内存可用。
 * <p>
 * 内存溢出是指当我们新建一个实力对象时，实例对象所需占用的内存空间大于堆的可用空间。
 * <p>
 * 可以采用调大-Xmx.
 * <p>
 * 测试时设置小一点JVM内存 java -Xms10M -Xmx10M -XX:-UseGCOverheadLimit
 * <p>
 * UseGCOverheadLimit: 通过统计GC时间来预测是否要OOM了，提前抛出异常，防止OOM发生。
 * <p>
 * 并行/并发回收器在GC回收时间过长时会抛出OutOfMemroyError。过长的定义是，超过98%的时间（-XX:GCTimeLimit指定GC时间所占总时间的百分比）用来做GC并且回收了不到2%的堆内存（GCHeapFreeLimit设置了一个下限，它指定了垃圾收集后应该有多大的空闲区域，这是一个相对于堆的总小大的百分比）。
 * <p>
 * https://www.iteye.com/blog/simon-fish-1631791
 */
public class HeapError {
	static List<String[]> list = Lists.newArrayList();
	
	
	public static void main(String[] args) {
		error1();
		error2();
	}

	/**
	 * Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at java.util.Arrays.copyOf(Unknown Source)
	at java.util.Arrays.copyOf(Unknown Source)
	at java.util.ArrayList.grow(Unknown Source)
	at java.util.ArrayList.ensureExplicitCapacity(Unknown Source)
	at java.util.ArrayList.ensureCapacityInternal(Unknown Source)
	at java.util.ArrayList.add(Unknown Source)
	 */
	private static void error1() {
		List<UUID> list = Lists.newArrayList();
		while (true) {
			list.add(UUID.randomUUID());
		}
	}

	private static void error2() {
		List<byte[]> list = Lists.newArrayList();
		while (true) {
			list.add(new byte[1000 * 1024 * 1024 * 1024]);
		}
	}
}
