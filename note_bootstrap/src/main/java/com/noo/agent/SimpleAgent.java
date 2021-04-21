package com.noo.agent;

import java.lang.instrument.Instrumentation;

/**https://www.cnblogs.com/rickiyang/p/11368932.html
 * https://www.cnblogs.com/yihuihui/p/12509416.html  -noverify 避免循环体无法输出
 * instrument的底层实现依赖于JVMTI(JVM Tool Interface)，它是JVM暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的，JVM每执行到一定的逻辑就会调用一些事件的回调接口（如果有的话），这些接口可以供开发者去扩展自己的逻辑。
 * <p> 需要在代理jar里的resources\MANIFEST.MF 声明 Premain-Class:MyAgent1 等
 * <p>
 * JVMTIAgent是一个利用JVMTI暴露出来的接口提供了代理启动时加载(agent on load)、代理通过attach形式加载(agent on  attach)和代理卸载(agent on unload)功能的动态库。而instrument agent可以理解为一类JVMTIAgent动态库，别名是JPLISAgent(Java Programming Language Instrumentation Services Agent)，也就是专门为java语言编写的插桩服务提供支持的代理。
 */
public class SimpleAgent {

	/**
	 * jvm 参数形式启动，运行premain方法 ：  java -javaagent:MyAgent1.jar -javaagent:MyAgent2.jar  -jar MyProgram.jar -javaagent:MyAgent3.jar  执行的是： MyAgent1.premain -> MyAgent2.premain -> MyProgram.main ；  放在main函数之后的premain是不会被执行的。
	 *
	 * manifest需要配置属性Premain-Class
	 *
	 * @param agentArgs
	 * @param inst
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("premain");
		customLogic(inst);
	}

	/**
	 * 动态 attach 方式启动，运行agentmain方法
	  *另外启用了一个jvm进程，找到需要attach的jvm进程，让它加载agentMain，那么agentMain就会被加载到对方jvm执行。arthas就是使用这种方式attach进jvm进程，开启一个socket然后进行目标jvm的监控。

	 * manifest需要配置属性Agent-Class
	 *
	 * @param agentArgs
	 * @param inst
	 */
	public static void agentmain(String agentArgs, Instrumentation inst) {
		System.out.println("agentmain");
		customLogic(inst);
	}

	/**
	 * 统计方法耗时
	 *
	 * @param inst
	 */
	private static void customLogic(Instrumentation inst) {
		inst.addTransformer(new CostTransformer(), true); // 注册一个Transformer，可以直接对类的字节码byte[]进行修改. 从此之后的类加载都会被Transformer拦截。
	}
}