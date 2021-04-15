package com.noob.testThink;

/**
 * 模拟this逃逸
 * 
 *
 */
public class TestEscape {
	// final常量会保证在构造器内完成初始化（但是仅限于未发生this逃逸的情况下，具体可以看多线程对final保证可见性的实现）
	final int i;
	// 尽管实例变量有初始值，但是还实例化完成
	int j = 0;
	static TestEscape obj;

	public TestEscape() {
		i = 1;
		j = 1;
		// 将this逃逸当作静态对象直接抛出给线程B访问
		obj = this;
	}

	public static void main(String[] args) {
		// 线程A：模拟构造器中this逃逸,将未构造完全对象引用抛出
		Thread threadA = new Thread(new Runnable() {

			@Override
			public void run() {
				obj = new TestEscape();
			}
		});

		// 线程B：读取对象引用，访问i/j变量
		Thread threadB = new Thread(new Runnable() {
			@Override
			public void run() {
				// 可能会发生初始化失败的情况解释：实例变量i的初始化被重排序到构造器外，此时1还未被初始化
				TestEscape objB = obj;
				try {
					System.out.println(objB.j);
				} catch (NullPointerException e) {
					System.out.println("发生空指针错误：普通变量j未被初始化");
				}
				try {
					System.out.println(objB.i);
				} catch (NullPointerException e) {
					System.out.println("发生空指针错误：final变量i未被初始化");
				}
			}
		});
		threadA.start(); // 这个线程还在初始化TestEscape 下一个线程已经开始访问， 就会异常。 主要还是看线程的完成执行的先后. 这个是模拟， 真实情况下不一定会发生
		threadB.start();
	}
}