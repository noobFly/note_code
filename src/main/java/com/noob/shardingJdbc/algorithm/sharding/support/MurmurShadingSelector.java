package com.noob.shardingJdbc.algorithm.sharding.support;

import java.util.List;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.noob.shardingJdbc.algorithm.sharding.AbstractShadingSelector;
import com.noob.shardingJdbc.algorithm.sharding.Sharding;

public class MurmurShadingSelector extends AbstractShadingSelector {
	private final static HashFunction murmur = Hashing.murmur3_32();

	public MurmurShadingSelector(List<Sharding> shardingList) {
		super(shardingList);
	}

	@Override
	protected long getHashCode(byte[] origin, int seed) {
		return Math.abs(murmur.hashBytes(origin).asInt());
	}
}
