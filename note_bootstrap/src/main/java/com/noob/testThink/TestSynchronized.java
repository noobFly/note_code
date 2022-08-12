package com.noob.testThink;

public class TestSynchronized {



    public void test() throws InterruptedException {
        synchronized (TestSynchronized.class) { // 全局唯一的锁 ，锁定的是metaspace区该class对象

            System.out.println("synchronized class");
            Thread.sleep(100000000);
        }
    }

    public synchronized static void test2() throws InterruptedException {
        System.out.println("synchronized static method");

    }

    public void test3() throws InterruptedException {
        synchronized (this) { // 对象锁时，对象不能为空！
            System.out.println("synchronized object");
            Thread.sleep(100000000);
        }

    }

    public synchronized void test4() throws InterruptedException {
        System.out.println("synchronized normal method");

    }

    public static void main(String args[]) throws InterruptedException {
        TestSynchronized a = new TestSynchronized();

        new Thread(() -> {
            try {
                a.test();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(100);

        new Thread(() -> {
            try {
                TestSynchronized.test2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(100);

        new Thread(() -> {
            try {
                a.test3();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(100);

        new Thread(() -> {
            try {
                a.test4();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(100);

        new Thread(() -> {
            try {
                new TestSynchronized().test3();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(100);
    }
}
