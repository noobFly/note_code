package com.noob.loadBalance.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.noob.util.MD5;

import lombok.Getter;

/**
 * Hash算法不仅要快且要变化足够激烈, 相似的字符串如“abc”和“abd”能够均匀散落在哈希环上
 *
 */
@Getter
public abstract class ConsistentHashSelector {
	private final TreeMap<Long, ServiceInvoker> virtualServices; // 每个服务实例的hash值映射
	private final int[] argumentIndex; // 指定索引位置的访问参数参与一致性算法
	private final int replicaNumber; // Node个数越大越平衡、越均匀
	private final int identityHashCode; // 调用者的id

	
	// dubbo://169.254.90.37:20880/service.DemoService?anyhost=true&application=srcAnalysisClient&check=false&dubbo=2.8.4&generic=false&interface=service.DemoService&loadbalance=consistenthash&methods=sayHello,retMap&pid=14648&sayHello.timeout=20000&side=consumer&timestamp=1493522325563
    //  dubbo在提供服务的URL中有hash.nodes指定虚拟节点数, hash.arguments指定方法传参中哪几位参与hash值计算
	public ConsistentHashSelector(List<ServiceInvoker> invokerList, int identityHashCode) {
		this.identityHashCode = identityHashCode;
		ServiceInvoker firstInvoker = invokerList.get(0);
		this.replicaNumber = firstInvoker.getNodes() < 1 ? 160 : firstInvoker.getNodes(); // 默认160虚拟Node  
		String[] index = Strings.isNullOrEmpty(firstInvoker.getHashArguments()) ? ServiceInvoker.DEFAULT_INDEX
				: firstInvoker.getHashArguments().split(",");
		argumentIndex = new int[index.length];
		for (int i = 0; i < index.length; i++) {
			argumentIndex[i] = Integer.parseInt(index[i]);
		}

		this.virtualServices = buildConsistentHashRing(invokerList, replicaNumber);

	}

	public static ketamaConsistentHashSelector newketamaSelector(List<ServiceInvoker> invokerList,
			int identityHashCode) {
		return new ketamaConsistentHashSelector(invokerList, identityHashCode);
	}

	public static MurmurConsistentHashSelector newMurmurSelector(List<ServiceInvoker> invokerList,
			int identityHashCode) {
		return new MurmurConsistentHashSelector(invokerList, identityHashCode);
	}

	public ServiceInvoker select(Object... params) {
		String key = toKey(params);
		byte[] digest = MD5.md5_16(key);
		return selectForKey(hash(digest, 0));
	}

	private String toKey(Object[] args) {
		StringBuilder buf = new StringBuilder();
		for (int i : argumentIndex) {
			if (i >= 0 && i < args.length) {
				buf.append(args[i]);
			}
		}
		return buf.toString();
	}

	private ServiceInvoker selectForKey(long hash) {
		Map.Entry<Long, ServiceInvoker> entry = virtualServices.tailMap(hash, true).firstEntry();
		if (entry == null) {
			entry = virtualServices.firstEntry(); // 超出范围为null则默认取第一个
		}
		return entry.getValue();
	}

	/**
	 * 与hash算法有关
	 * 
	 * @param invokerList   服务提供者
	 * @param replicaNumber 虚拟节点个数
	 * @return 每个服务对应的hash值映射
	 */
	protected abstract TreeMap<Long, ServiceInvoker> buildConsistentHashRing(List<ServiceInvoker> invokerList,
			int replicaNumber);

	/**
	 * 具体的hash算法
	 * 
	 * @param digest Md5 处理key值后的byte[]
	 * @param offset 偏移量
	 * @return hash值
	 */
	protected abstract long hash(byte[] digest, int offset);
  
	public static class ketamaConsistentHashSelector extends ConsistentHashSelector {

		public ketamaConsistentHashSelector(List<ServiceInvoker> invokerList, int identityHashCode) {
			super(invokerList, identityHashCode);
		}

		// 按每组4个来构建虚拟服务Node. 因为 Md5是一个16字节长度的数组，将16字节的数组每四个字节一组，分别对应一个虚拟节点  dubbo使用
		@Override
		public long hash(byte[] digest, int offset) {
			return (((long) (digest[3 + offset * 4] & 0xFF) << 24) | ((long) (digest[2 + offset * 4] & 0xFF) << 16)
					| ((long) (digest[1 + offset * 4] & 0xFF) << 8) | (digest[offset * 4] & 0xFF)) & 0xFFFFFFFFL;
		}

		@Override
		public TreeMap<Long, ServiceInvoker> buildConsistentHashRing(List<ServiceInvoker> invokerList,
				int replicaNumber) {
			TreeMap<Long, ServiceInvoker> virtualServices = new TreeMap<Long, ServiceInvoker>();
			// 按每组4个来构建虚拟服务Node. 因为 Md5后的hash值是一个16字节长度的数组，将16字节的数组每四个字节一组，分别对应一个虚拟节点
			for (ServiceInvoker invoker : invokerList) {
				for (int i = 0; i < replicaNumber / 4; i++) {
					byte[] digest = MD5.md5_16(invoker.getServiceIp() + i); // 增加编号来作标识Node
					for (int offset = 0; offset < 4; offset++) {
						virtualServices.put(hash(digest, offset), invoker);
					}
				}
			}
			return virtualServices;
		}

	}

	public static class MurmurConsistentHashSelector extends ConsistentHashSelector {

		public MurmurConsistentHashSelector(List<ServiceInvoker> invokerList, int identityHashCode) {
			super(invokerList, identityHashCode);
		}

		@Override
		protected TreeMap<Long, ServiceInvoker> buildConsistentHashRing(List<ServiceInvoker> invokerList,
				int replicaNumber) {
			TreeMap<Long, ServiceInvoker> virtualServices = new TreeMap<Long, ServiceInvoker>();
			for (ServiceInvoker invoker : invokerList) {
				for (int i = 0; i < replicaNumber; i++) {
					byte[] digest = MD5.md5_16(invoker.getServiceIp() + i); // 增加编号来作标识Node
					virtualServices.put(hash(digest, 0), invoker);
				}
			}
			return virtualServices;
		}

		@Override
		protected long hash(byte[] digest, int offset) {
			return Math.abs(Hashing.murmur3_32().hashBytes(digest).asInt());
		}
	}
}
