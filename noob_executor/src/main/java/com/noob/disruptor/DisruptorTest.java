package com.noob.disruptor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DisruptorTest {
  public static void main(String[] args) {
	  execute();
  }
	public static void execute() {

		Disruptor<DataEvent> disruptor = new Disruptor<DataEvent>(new DataEventFactory(), CompareTest.SIZE,
				new ThreadFactory() {
					AtomicInteger count = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable eventProcessor) {
						CompareTest.println("EventProcessor wrapper");// 对事件处理总线的封装
						Thread thread = new Thread(eventProcessor);
						thread.setName("EventProcessor" + count.incrementAndGet());
						return thread;
					}
				});
		/**
		 * 创建EventProcessors<Runnable>.
		 * 子过程Disruptor.checkNotStarted()事件处理handler必须在启动之前绑定.
		 */
		disruptor.handleEventsWith(new DataEventHandler("dataEventHandler1"))
		.thenHandleEventsWithWorkerPool(new DataWorkHandler("dataWorkHandler3"));
		// disruptor.handleExceptionsFor(null);
		// disruptor.handleEventsWithWorkerPool(null);
		disruptor.start();
		CompareTest.println("disruptor start success！");

		RingBuffer<DataEvent> ringBuffer = disruptor.getRingBuffer();
		DataProducer producer = new DataProducer(ringBuffer);
		DataEventProducerWithTranslator translator = new DataEventProducerWithTranslator(ringBuffer);
		long start = System.currentTimeMillis();
		for (int l = 0; l < CompareTest.THREAD; l++) {
			new Thread(() -> {
				for (int m = 0; m < CompareTest.PER; m++) {
					producer.onData(start);
					// translator.onData(start); 推荐用这种方式做。
				}
			}).start();
		}
	}
}
