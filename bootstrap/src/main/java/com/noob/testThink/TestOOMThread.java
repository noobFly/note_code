package com.noob.testThink;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * 验证: 当某个线程OOM 或者是StackOverflowError 后，另外的线程依然能够工作!
 * 发生OOM的线程一般情况下会死亡，也就是会被终结掉，该线程持有的对象占用的heap都会被gc了，释放内存。
 * 因为发生OOM之前要进行gc，就算其他线程能够正常工作，也会因为频繁gc产生较大的影响。
 */
public class TestOOMThread {
 public static void test() {
	 test();
 }
    public static void main(String[] args) {
        new Thread(() -> {
            List<byte[]> list = new ArrayList<byte[]>();
            while (true) {
                System.out.println(new Date().toString() + Thread.currentThread() + "输出");
                byte[] b = new byte[1024 * 1024 * 1024];
                list.add(b);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 线程二
        new Thread(() -> {
            while (true) {
                System.out.println(new Date().toString() + Thread.currentThread() + "输出");
                try {
                	test();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        // 线程三
        new Thread(() -> {
            while (true) {
                System.out.println(new Date().toString() + Thread.currentThread() + "输出");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

/**

Fri Apr 09 16:30:05 CST 2021Thread[Thread-0,5,main]输出
Fri Apr 09 16:30:05 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:30:05 CST 2021Thread[Thread-1,5,main]输出
Exception in thread "Thread-1" java.lang.StackOverflowError
	at com.noob.testThink.JvmThread.test(JvmThread.java:9)
	at com.noob.testThink.JvmThread.test(JvmThread.java:9)
	at com.noob.testThink.JvmThread.test(JvmThread.java:9)
	Fri Apr 09 16:30:27 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:30:27 CST 2021Thread[Thread-0,5,main]输出
Fri Apr 09 16:30:28 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:30:28 CST 2021Thread[Thread-0,5,main]输出
Fri Apr 09 16:30:27 CST 2021Thread[Thread-0,5,main]输出
Fri Apr 09 16:30:28 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:30:28 CST 2021Thread[Thread-0,5,main]输出
Exception in thread "Thread-0" java.lang.OutOfMemoryError: Java heap space
	at com.noob.testThink.JvmThread.lambda$0(JvmThread.java:16)
	at com.noob.testThink.JvmThread$$Lambda$1/531885035.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:748)
Fri Apr 09 16:32:08 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:32:09 CST 2021Thread[Thread-2,5,main]输出
Fri Apr 09 16:32:10 CST 2021Thread[Thread-2,5,main]输出
**/
