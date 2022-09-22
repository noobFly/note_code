package com.noob.disruptor.model;

import java.util.concurrent.atomic.AtomicLong;

import com.lmax.disruptor.WorkHandler;
import com.noob.disruptor.CompareTest;

public class DataWorkHandler implements WorkHandler<DataEvent> {
	public AtomicLong count = new AtomicLong(0);
	public String name = null;

	public DataWorkHandler(String name) {
		this.name = name;
	}


	@Override
	public void onEvent(DataEvent event) throws Exception {
		Thread.sleep(name.contentEquals("dataEventHandler1") ? 1 : 1);
		CompareTest.println("handlerName: " + name + " 处理的sequence：" + event.getSequence()
				+ " count：" + count.incrementAndGet() + "  Disruptor 总耗时："
				+ (System.currentTimeMillis() - event.getStartTime()));
	}

}
