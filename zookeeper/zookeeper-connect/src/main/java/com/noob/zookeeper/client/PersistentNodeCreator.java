package com.noob.zookeeper.client;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersistentNodeCreator {

	private final static String create_tip = " node:【{}】 is created.";
	private final static String exception_tip =
			"when create root node happen NodeExistsException: {}. 【{}】";
	private final static String has_created = "the node has created by other zkClient.";
	private final static String not_created = "but the node really not be created!";
	private final static String slant = "/";
	public static void create(String nodePath, ZooKeeper zooKeeper) throws Exception {
		if (nodePath.lastIndexOf(slant) > 0) {
			int start = 0;
			int end = 0;
			while (end < nodePath.length()) {
				end = nodePath.indexOf(slant, start + 1);
				if (end < 0) end = nodePath.length();
				createNode(zooKeeper, nodePath.substring(0, end));
				start = end;
			}
		} else {
			createNode(zooKeeper, nodePath);
		}

	}

	/**
	 * 在并发状态下需要再次判定是否已经创建好Node
	 *
	 * @param zooKeeper
	 * @param nodePath  创建的Node路径
	 * @throws Exception
	 */
	private static void createNode(ZooKeeper zooKeeper, String nodePath) throws Exception {
		//对于exists可以自定义watcher，如果使用zk.exists("/root", true);默认会冲洗注册default watcher实现监控。
		Stat stat = zooKeeper.exists(nodePath, false);
		if (stat == null) {
			try {
				String createNode = zooKeeper.create(nodePath, new byte[0],
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);// 永久性节点
				log.info(create_tip, createNode);
			} catch (KeeperException.NodeExistsException e) {
				stat = zooKeeper.exists(nodePath, false);
				String tip = stat != null ? has_created : not_created;
				log.error(exception_tip, e.getMessage(), tip);
			} catch (Exception e) {
				throw e;
			}
		}
	}

}
