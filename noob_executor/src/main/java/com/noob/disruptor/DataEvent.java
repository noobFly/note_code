package com.noob.disruptor;

import lombok.Getter;
import lombok.Setter;

/**
 * 事件实例封装 业务数据传递对象
 * 
 * @author admin
 *
 */
@Getter
@Setter
public class DataEvent {
	private long startTime;

	private long sequence;

}
