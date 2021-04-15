package com.noob.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

/**
 * 获取下一个事件槽并发布事件（发布事件的时候要使用try/finnally保证事件一定会被发布）。
 * 如果我们使用RingBuffer.next()获取一个事件槽，那么一定要发布对应的事件。如果不能发布事件，那么就会引起Disruptor状态的混乱
 * 。尤其是在多个事件生产者的情况下会导致事件消费者失速，从而不得不重启应用才能会恢复。
 * 
 * @author admin
 *
 */
public class DataEventProducerWithTranslator {
	private final RingBuffer<DataEvent> ringBuffer;

	// 一个translator可以看做一个事件初始化器，publicEvent方法会调用它
	// 填充Event
	private static final EventTranslatorOneArg<DataEvent, Long> TRANSLATOR = new EventTranslatorOneArg<DataEvent, Long>() {
		public void translateTo(DataEvent event, long sequence, Long startTime) {
			event.setStartTime(startTime);
		}
	};

	public DataEventProducerWithTranslator(RingBuffer<DataEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void onData(Long bb) {
		ringBuffer.publishEvent(TRANSLATOR, bb);
		// 当前还是生产者线程
	//	CompareTest.println(Thread.currentThread().getName() + " pulishEvent end!");
		
	}

}