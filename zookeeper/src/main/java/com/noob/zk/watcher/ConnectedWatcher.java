package com.noob.zk.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * zookeeper是异步连接的，new出ZooKeeper实例时只是建立了与服务端之间的会话，
 * 此时TCP连接可能还未建立完成避免连接建立完成之前就发出ZooKeeper操作命令get/create/exist出现ConnectionLoss异常。
 * 当客户端与服务端连接建立之后客户端会收到一个SyncConnected事件。
 * <p>
 * Created by noob on 2017/4/6.
 */
public class ConnectedWatcher implements Watcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CountDownLatch connectedLatch;


    public ConnectedWatcher() {
        this.connectedLatch = new CountDownLatch(1);

    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("connect zk server response watchedEvent：{}.", event.toString());
        if (event.getState() == Event.KeeperState.SyncConnected) {
            if (connectedLatch != null) connectedLatch.countDown();
        }
    }

    public CountDownLatch getConnectedLatch() {
        return this.connectedLatch;
    }
}
