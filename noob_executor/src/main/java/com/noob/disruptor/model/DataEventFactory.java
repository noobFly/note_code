package com.noob.disruptor.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.EventFactory;
import com.noob.disruptor.CompareTest;

/*初始化 Disruptor 时，给ringBuffer每一个初始位置封装一个内容对象
 */
public class DataEventFactory implements EventFactory {
	private AtomicInteger count = new AtomicInteger(0);

	@Override
	public Object newInstance() {
		CompareTest.println("dataEvent newInstance fill capacity: " + count.incrementAndGet());
		return new DataEvent();
	}

}
