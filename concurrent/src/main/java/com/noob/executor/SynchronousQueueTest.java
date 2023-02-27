package com.noob.executor;

import java.util.concurrent.SynchronousQueue;

/**
 * 没有所谓的容量!
 * 消费者与生产者是1：1的关系， 如果缺失另外一方，将阻塞
 * 如果一个take或者put线程进来发现有同类的take或者put线程在阻塞中，那么线程会排到后面，直到有不同类的线程进来然后匹配其中一个线程。
 */
public class SynchronousQueueTest {

    public static void main(String[] args) {
        SynchronousQueue queue = new SynchronousQueue();


        new Thread(() -> {
            try {
                queue.put(new Object());
                System.out.println("put success");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                System.out.println(queue.take());
                System.out.println("take success");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
