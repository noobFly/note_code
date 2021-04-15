package com.noob.disruptor;

import com.lmax.disruptor.RingBuffer;

public class DataProducer {
	private final RingBuffer<DataEvent> ringBuffer;

	public DataProducer(RingBuffer<DataEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * 当前还是生产线程
	 * <p>
	 * onData用来发布事件，每调用一次就发布一次事件事件 它的参数会通过事件传递给消费者
	 * 
	 * @param data
	 */
	public void onData(long data) {//
		// 可以把ringBuffer看做一个事件队列，那么next就是得到下面一个事件槽, 若没有空闲的时间槽则阻塞
		long sequence = ringBuffer.next();
		 CompareTest.println("生产置入sequence:" + sequence);
		try {
			// 用上面的索引取出一个空的事件用于填充
			DataEvent event = ringBuffer.get(sequence);// for the sequence
			event.setStartTime(data);
			event.setSequence(sequence);
		} finally {
			// 发布事件
			ringBuffer.publish(sequence);
		}
	}
}