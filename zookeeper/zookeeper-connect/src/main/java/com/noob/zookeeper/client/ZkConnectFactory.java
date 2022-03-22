package com.noob.zookeeper.client;

import com.noob.zookeeper.client.watcher.ConnectedWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.TimeUnit;

/**
 * 初始化zk， 创建基础永久节点。  创建永久节点最好是在web容易
 * Created by noob on 2017/4/5.
 */
@Slf4j
public class ZkConnectFactory {

    private final String address = "127.0.0.1:2181";
    private final int sessionTimeout = 30000;
    private final int waitTime = 1000;
    public static final String slant = "/";

    /**
     * @param nodePath 需要创建的节点路径
     * @return
     */
    public ZooKeeper init(String nodePath) throws Exception {
        ZooKeeper zooKeeper = null;

        if (nodePath != null && nodePath.length() > 1 && nodePath.contains(slant)) {

            try {

                ConnectedWatcher defaultWatcher = new ConnectedWatcher();
                zooKeeper = new ZooKeeper(address, sessionTimeout, defaultWatcher); //指定defaultWatcher 存储在ZKWatchManager.defaultWatcher 中
                defaultWatcher.getConnectedLatch().await(waitTime, TimeUnit.MILLISECONDS); // 此处设置超时，防止死锁
                PersistentNodeCreator.create(nodePath, zooKeeper);
            } catch (Exception e) {
                log.error("create the persistent basic node exception.", e);
                throw e;
            }
        }

        return zooKeeper;
    }

}
