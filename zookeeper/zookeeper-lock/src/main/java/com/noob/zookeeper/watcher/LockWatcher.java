package com.noob.zookeeper.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 等待已经获取到锁的进程释放的监控
 * Created by noob on 2017/4/5.
 */
public class LockWatcher implements Watcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private CountDownLatch latch;

    public LockWatcher() {
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("other thread release lock response: {}", event.toString());
        if (this.latch != null) {
            this.latch.countDown();
        }
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public CountDownLatch getLatch() {
        return this.latch;
    }
}
