package com.noob.test.zookeeper;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.Stat;

/**
 * delete node with auth
 */
public class AuthTest {

	private static String path = "/noob";
	private static String address = "127.0.0.1:2181";
	private static String scheme = "digest";
	private static String password = "password";

	public void deleteAuth() throws Exception {
		PathUtils.validatePath(path);
		ZooKeeper z = new ZooKeeper(address, 1000, null);
		z.addAuthInfo(scheme, password.getBytes(Charset.forName("UTF-8")));
		deleteIterator(path, z);
	}

	private void deleteIterator(String path, ZooKeeper z) throws Exception {
		if (path != null) {
			List<String> children = z.getChildren(path, false);
			while (children != null && children.size() > 0) {
				for (String str : children) {
					deleteIterator(path + "/" + str, z);
				}
			}
			Stat stat = new Stat();
			z.getACL(path, stat);
			System.out.println(stat);
			z.delete(path, -1);
		}

	}

}
