package com.noob.overflow;

/**
 * 栈（JVM Stack）存放主要是栈帧( 局部变量表, 操作数栈 , 动态链接 , 方法出口信息 )的地方。注意区分栈和栈帧：栈里包含栈帧。
 * <P>
 * 栈溢出 (  -Xss128k 设置每个线程的栈大小)
 * <P>
 * StackOverflowError(方法调用层次太深，内存不够新建栈帧)
 * <P>
 * OutOfMemoryError（线程太多，限制创建线程 ulimit- n）
 */
public class StackError {
	static long i = 0L;
/**
 * Exception in thread "main" java.lang.StackOverflowError
	at sun.nio.cs.UTF_8$Encoder.encodeLoop(Unknown Source)
	at java.nio.charset.CharsetEncoder.encode(Unknown Source)
	at sun.nio.cs.StreamEncoder.implWrite(Unknown Source)
	at sun.nio.cs.StreamEncoder.write(Unknown Source)
	at java.io.OutputStreamWriter.write(Unknown Source)
	at java.io.BufferedWriter.flushBuffer(Unknown Source)
	at java.io.PrintStream.newLine(Unknown Source)
	at java.io.PrintStream.println(Unknown Source)
	at com.noob.overflow.StackError.StackOverflowError(StackError.java:28)
 */
	public static void StackOverflowError() {
		System.out.println(i++);
		StackOverflowError();
	}

	/**
	 * Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
    at java.lang.Thread.start0(Native Method)
    at java.lang.Thread.start(Thread.java:597)
	 */
	public static void OutOfMemoryError() {
		while (true) {
			new Thread(() -> {
				try {
					System.out.println(1);
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	
	public static void main(String[] args) {
		try {
			OutOfMemoryError();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			StackOverflowError();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	}
}
