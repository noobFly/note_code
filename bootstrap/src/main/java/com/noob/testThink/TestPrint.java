package com.noob.testThink;

import java.util.function.Predicate;

public class TestPrint {

	private static Integer index = 0;
    private static Integer max = 6;
    private static Object lock = new Object();

    public static void main(String[] args) {

        Thread a = getThread(i -> i % 3 == 0, "A");
        Thread b = getThread(i -> i % 3 == 1, "B");
        Thread c = getThread(i -> i % 3 == 2, "C");
        a.start();
        b.start();
        c.start();

    }

    private static Thread getThread(Predicate<Integer> condition, String value) {
        return new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    while (!condition.test(index)) {
                        try {
                            //如果已经不需要继续，直接return,避免继续等待
                            if (index >= max) {
                                return;
                            }
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    //如果已经不需要继续，通知所有wait的线程收拾东西回家后，然后自己回家
                    if (index >= max) {
                        lock.notifyAll();
                        return;
                    }

                    System.out.printf("index:%s,value:%s\n", index, value);
                    index++;
                    lock.notifyAll();
                }
            }
        });
    }

}
