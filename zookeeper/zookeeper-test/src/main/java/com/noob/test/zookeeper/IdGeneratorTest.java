package com.noob.test.zookeeper;

import com.noob.zookeeper.IdGenerator;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;


public class IdGeneratorTest {

    public void test() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        int count = 3;
        CountDownLatch threadLatch = new CountDownLatch(count);

        CountDownLatch loopLatch = new CountDownLatch(1);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    loopLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.add(IdGenerator.newInstance("order", "cs", false).generator());
                threadLatch.countDown();
            }).start();

        }

        loopLatch.countDown();

        try {
            threadLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(list.size() + list.toString());
    }
}
