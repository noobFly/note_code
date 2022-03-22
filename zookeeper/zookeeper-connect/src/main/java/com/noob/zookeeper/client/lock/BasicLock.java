package com.noob.zookeeper.client.lock;

import java.util.concurrent.locks.Lock;

/**
 * Created by noob on 2017/4/6.
 */
public interface BasicLock extends Lock {

    String domain = "/locks";//锁域
    String lock_tip = "_lock_";// 锁类型节点路径校验
    int waitTime = 30000;

}
