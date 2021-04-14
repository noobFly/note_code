package com.noob.shardingJdbc.algorithm.sharding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 参考：https://www.cnkirito.moe/consistent-hash-lb/
 * @author fengjie
 *
 */
public abstract class AbstractShadingSelector implements ShadingSelector {
	private final TreeMap<Long, Sharding> ring;
	protected final static String VIRTUAL_NODE_SUFFIX = "-";
	protected final static int VIRTUAL_NODE_SIZE = 1024; //越大越平衡、越均匀

	private static MessageDigest md5Digest;

	static {
		try {
			md5Digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("未支持MD5算法", e);
		}
	}

	public AbstractShadingSelector(List<Sharding> shardingList) {
		ring = buildConsistentHashRing(shardingList);
	}

	@Override
	public Sharding select(String key) {
		Map.Entry<Long, Sharding> locateEntry = ring.ceilingEntry(getHashCode(computeMd5(key), 0));
        if (locateEntry == null) {
            // 想象成一个环，超过尾部则取第一个 key
            locateEntry = ring.firstEntry();
        }
        return locateEntry.getValue();
	}

	protected TreeMap<Long, Sharding> buildConsistentHashRing(List<Sharding> shardingList) {
		TreeMap<Long, Sharding> virtualNodeRing = new TreeMap<>();
		for (Sharding sharding : shardingList) {
			for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
				byte[] digest = computeMd5(sharding.getShadingCode() + VIRTUAL_NODE_SUFFIX + i);
				virtualNodeRing.put(getHashCode(digest, 0), sharding);
			}
		}
		return virtualNodeRing;
	}

	protected abstract long getHashCode(byte[] origin, int seed);

	protected static byte[] computeMd5(String k) {
		MessageDigest md5;
		try {
			md5 = (MessageDigest) md5Digest.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("未支持MD5克隆", e);
		}
		md5.update(k.getBytes());
		return md5.digest();
	}
}
