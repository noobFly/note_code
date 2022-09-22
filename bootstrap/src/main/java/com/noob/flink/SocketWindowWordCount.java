package com.noob.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.api.windowing.time.Time;

public class SocketWindowWordCount {

	public static void main(String[] args) throws Exception {

		// 创建 execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<String> text = env.socketTextStream("127.0.0.1", 8080, "\n"); //通过连接 socket 获取输入数据。 连接的是一个持续输入数据源, rabbit、 kafka 等

		// 解析数据，按 word 分组，开窗，聚合
		DataStream<Tuple2<String, Integer>> windowCounts = text.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
			public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
				for (String word : value.split("\\s")) {//分词符合
					out.collect(Tuple2.of(word, 1)); 
				}

			}
		}).keyBy(0).timeWindow(Time.seconds(1)).sum(1);// 统计时间窗口内的分词个数。 去掉timeWindow(Time.seconds(1))会统计所有

		// 将结果打印到控制台，注意这里使用的是单线程打印，而非多线程
		windowCounts.print().setParallelism(1);

		env.execute("Socket Window WordCount");
	}
}