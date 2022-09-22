package com.noob.shardingJdbc.algorithm.sharding.support;

import java.util.List;
import java.util.TreeMap;

import com.noob.shardingJdbc.algorithm.sharding.AbstractShadingSelector;
import com.noob.shardingJdbc.algorithm.sharding.Sharding;

/**
 * 基于Ketama一致性hash算法的实现<br>
 * 参考：https://www.cnkirito.moe/consistent-hash-lb/
 *
 */
public class KetamaShadingSelector extends AbstractShadingSelector {

	public KetamaShadingSelector(List<Sharding> shardingList) {
		super(shardingList);
	}

	@Override
	protected long getHashCode(byte[] origin, int seed) {
		Long k = ((long) (origin[3 + seed * 4] & 0xFF)<< 24)
                | ((long) (origin[2 + seed * 4] & 0xFF)<< 16)
                | ((long) (origin[1 + seed * 4] & 0xFF)<< 8)
                | (origin[seed * 4] & 0xFF);
		return k;
	}

	@Override
	protected TreeMap<Long, Sharding> buildConsistentHashRing(List<Sharding> shardingList) {
		TreeMap<Long, Sharding> virtualNodeRing = new TreeMap<>();
		for (Sharding sharding : shardingList) {
			for (int i = 0; i < VIRTUAL_NODE_SIZE / 4; i++) {
				byte[] digest = computeMd5(sharding.getShadingCode() + VIRTUAL_NODE_SUFFIX + i);
				for (int h = 0; h < 4; h++) {
					virtualNodeRing.put(getHashCode(digest, h), sharding);
				}
			}
		}
		return virtualNodeRing;
	}
}
