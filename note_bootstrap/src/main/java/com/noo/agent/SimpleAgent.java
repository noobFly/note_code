package com.noo.agent;

import java.lang.instrument.Instrumentation;

/**https://www.cnblogs.com/rickiyang/p/11368932.html
 * https://www.cnblogs.com/yihuihui/p/12509416.html  -noverify 避免循环体无法输出
 * instrument的底层实现依赖于JVMTI(JVM Tool Interface)，它是JVM暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的，JVM每执行到一定的逻辑就会调用一些事件的回调接口（如果有的话），这些接口可以供开发者去扩展自己的逻辑。
 * <p> 需要在代理jar里的resources\MANIFEST.MF 定义
 * <p>
 * JVMTIAgent是一个利用JVMTI暴露出来的接口提供了代理启动时加载(agent on load)、代理通过attach形式加载(agent on  attach)和代理卸载(agent on unload)功能的动态库。而instrument agent可以理解为一类JVMTIAgent动态库，别名是JPLISAgent(Java Programming Language Instrumentation Services Agent)，也就是专门为java语言编写的插桩服务提供支持的代理。
 */
public class SimpleAgent {

	/**
	 * jvm 参数形式启动，运行此方法
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
	 * 动态 attach 方式启动，运行此方法
	 *
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
		inst.addTransformer(new CostTransformer(), true); // 注册一个Transformer，可以直接对类的字节码byte[]进行修改.
															// 从此之后的类加载都会被Transformer拦截。
	}
}