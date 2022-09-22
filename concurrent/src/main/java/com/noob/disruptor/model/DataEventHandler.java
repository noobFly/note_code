package com.noob.disruptor.model;

import java.util.concurrent.atomic.AtomicLong;

import com.lmax.disruptor.EventHandler;
import com.noob.disruptor.CompareTest;

/**
 * 对指定事件的处理过程
 *
 */

public class DataEventHandler implements EventHandler<DataEvent> {
	public AtomicLong count = new AtomicLong(0);
	public String name = null;

	public DataEventHandler(String name) {
		this.name = name;
	}

	@Override
	public void onEvent(DataEvent event, long sequence, boolean endOfBatch) throws Exception {
		Thread.sleep(name.contentEquals("dataEventHandler1") ? 1 : 1);
		CompareTest.println("handlerName: " + name + " 处理的sequence：" + sequence
				+ " count：" + count.incrementAndGet() + "  Disruptor 总耗时："
				+ (System.currentTimeMillis() - event.getStartTime()));
	}

}
