package com.noob.zookeeper.lock;

import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.noob.zookeeper.client.ZkConnectFactory;
import com.noob.zookeeper.client.lock.BasicLock;

import lombok.extern.slf4j.Slf4j;

/**
 *  import org.aopalliance.intercept.MethodInterceptor;
 * public class TaskUnblockInterceptor implements MethodInterceptor {
 *         public Object invoke(MethodInvocation invocation) {
 *            return  invocation.proceed();
 *         }
 * }
 *
 * <bean id="taskUnblockInterceptor" class="com.iboxpay.loan.core.task.lock.TaskUnblockInterceptor" />
 * <aop:config proxy-target-class="true">
 * <aop:pointcut id="taskUnblockPointcut" expression="execution(* com.iboxpay.loan.core.task..*.*(..)) and
 * @annotation(com.iboxpay.loan.core.task.lock.TaskUnblock)"/>
 *<aop:advisor advice-ref="taskUnblockInterceptor" pointcut-ref="taskUnblockPointcut" />
 * </aop:config>
 *
 * Created by noob on 2017/4/17.
 */
@Slf4j
public class UnBlockedLock {

	private final String topic = "【获取非阻塞式锁处理】";
	private ZooKeeper zk;
	private String root = null; // 锁节点的根路径
	private String lockType;//竞争锁的类型
	public static final String slant = "/";

	public UnBlockedLock(String businessDomain, String lockType) {

		this.lockType = lockType;
		this.root = businessDomain.concat(BasicLock.domain);

		try {
			zk = new ZkConnectFactory().init(root);
		} catch (Exception e) {

		}
	}

	/**
	 * 当前一个任务获取锁执行未完成时，新一轮的任务又开始从开始抢占锁。
	 * 1、 在判定获取到锁后，间隔一定时间后立刻把zk close。尽快把节点释放。（任务的周期间隔不会极短）
	 * 2、zk 在创建和删除临时节点时，都会导致cversion和pzxid变动。所以谁获取到锁，谁先变更节点数据版本
	 *
	 */
	public boolean lock() {
		boolean lock = false;

		if (zk != null && StringUtils.isNotBlank(lockType)) {
			String ip = null;
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				log.error("{} 获取当前机器IP 异常. ", topic, e);
			}
			log.info("{} 开始， 机器IP:{}, lockNode:{}", topic, ip, lockType);

			String path = null;

			try {

				Stat stat = zk.exists(root, false);
				int version = stat.getVersion();// 节点数据版本

				path = root.concat(slant).concat(lockType)
						.concat(String.valueOf(version));
				String currentLockNode = zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);

				log.info("节点：【{}】 创建成功. ", currentLockNode);

				try {
					zk.setData(root, new byte[0], -1);// 不校验版本号
				} catch (Exception e) {
					log.info("节点：【{}】 创建成功获取锁后修改根节点：【{}】 异常.", path, root, e);
				}

				lock = true;
			} catch (Exception e) {
				log.error("{} 【节点：{} 创建失败. 】 exception. 当前服务实例没有获取执行权限.", topic, path, e);

			} finally {

				try {
					if (path != null) {
						zk.delete(path, -1);
					}
					zk.close();
				} catch (Exception e) {
					log.error("close zk exception", e);
				}
			}
		}

		return lock;
	}
}
