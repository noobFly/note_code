package com.noob.zk.lock;

import com.noob.zk.ZkConnectFactory;
import com.noob.zk.watcher.LockWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

/**
 * Created by noob on 2017/4/5.
 */
@Slf4j
public class DistributedLock implements BasicLock {

    private String root = null; // 锁节点的根路径

    private ZooKeeper zk;

    private String lockType;//竞争锁的类型
    private String currentLockNode;//当前线程创建的锁
    private String waitNode;//等待前一个锁节点

    private Exception exception = null;
    public static final String slant = "/";

    private DistributedLock(String businessDomain, String lockType) {

        this.lockType = lockType;
        this.root = businessDomain.concat(BasicLock.domain);

        try {
            zk = new ZkConnectFactory().init(root);
        } catch (Exception e) {
            exception = e;
        }
    }

    public static DistributedLock blockInstance(String domain, String lockType) {
        return new DistributedLock(domain, lockType);
    }

    @Override
    public void lock() {

        if (exception != null) throw new LockException(exception);

        if (zk == null) throw new LockException(new Exception("zkClient init fail. "));

        try {
            if (this.tryLock()) {

                log.info("Thread 【{}】 【{}】 get lock success.", Thread.currentThread().getId(),
                        currentLockNode);
            } else {
                waitForLock(waitNode, waitTime);//等待锁
            }
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    @Override
    public boolean tryLock() {
        boolean result = false;
        if (lockType.contains(lock_tip)) throw new LockException("lockType valid fail! ");
        try {
            //创建临时子节点 [business]/[domain]/[lockType]/[lock_tip]
            currentLockNode = zk.create(root + slant + lockType + lock_tip, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info("node：【{}】 is created.", currentLockNode);
            //取出所有子节点
            List<String> subNodes = zk.getChildren(root, false);
            if (subNodes != null && !subNodes.isEmpty()) {
                List<String> lockObjNodes = subNodes.stream().filter(node -> {
                            String _node = node.split(lock_tip)[0];
                            return _node.equals(lockType);
                        }

                ).sorted().collect(Collectors.toList()); //子节点指定锁类型结合

                String lockedNode = lockObjNodes.get(0);
                log.info("当前节点为：【{}】, 当前获取到锁的节点为：【{}】.", currentLockNode, lockedNode);
                if (currentLockNode.equals(root + slant + lockedNode)) {
                    //如果是最小的节点,则表示取得锁
                    result = true;
                } else {
                    //如果不是最小的节点，找到比自己小1的节点
                    String sortNumber = currentLockNode
                            .substring(currentLockNode.lastIndexOf(slant) + 1); // 当前创建节点的序号
                    waitNode = lockObjNodes
                            .get(Collections.binarySearch(lockObjNodes, sortNumber) - 1);

                }
            }

        } catch (Exception e) {
            throw new LockException(e);
        }
        return result;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean result = false;
        try {
            result = this.tryLock() ? true : waitForLock(waitNode, time);
        } catch (Exception e) {
            log.error("try lock exception.", e);
        }
        return result;
    }

    /**
     * 判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听,当节点变化时触发watcher
     * TODO  等待最长时间释放不一定是获取到锁权限
     *
     * @param node 当前节点锁需要关注的前一节点
     * @param time 获取锁超时时间
     * @return
     * @throws Exception
     */
    private boolean waitForLock(String node, long time) throws Exception {
        boolean result = true;

        //等待已经获取到锁的进程释放的监控
        LockWatcher lockWatcher = new LockWatcher();

        Stat stat = zk.exists(root + slant + node, lockWatcher);

        if (stat != null) {

            log.info("Thread 【{}】, waiting for  pre-Lock:【{}】.", Thread.currentThread().getId(),
                    root + slant + node);

            lockWatcher.getLatch().await(time, TimeUnit.MILLISECONDS);

        }

        lockWatcher.setLatch(null);

        return result;

    }

    @Override
    public void unlock() {
        try {
            log.info("release  lock node: 【{}】", currentLockNode);
            if (zk != null) {
                if (currentLockNode != null) {
                    zk.delete(currentLockNode, -1);
                    currentLockNode = null;
                    waitNode = null;
                }
                zk.close();
            }
        } catch (Exception e) {
            log.error("release lock exception.", e);
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
