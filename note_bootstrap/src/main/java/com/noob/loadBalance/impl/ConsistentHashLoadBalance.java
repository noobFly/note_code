package com.noob.loadBalance.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.noob.loadBalance.AbstractLoadBalance;

/**
 * 一致性hash 参考： https://www.cnkirito.moe/consistent-hash-lb/
 * <p>
 * 需要考虑： 实现复杂程度、 分布均匀程度、 哈希碰撞概率、 性能
 * <p>
 * 如果用 % 取模 来处理, 算法伸缩性很差， 当服务节点数发生变化时, 与服务器的映射关系会大量失效。
 * <p>
 * 一致性hash则利用hash环对其进行了改进。 一致性哈希将整个哈希值空间组织成一个虚拟的圆环。 服务Node的hash是该圆环上的定位。
 * <p>
 * 也可以理解为横轴， Node的hash定位在一段区间上，若请求hash超出这个范围内默认返回第一个Node。
 * <p>
 * 用相同的函数Hash计算出哈希值，并确定此数据在环上的位置，从此位置沿环顺时针“行走”，第一台遇到的服务器就是其应该定位到的服务器。
 * 如果服务节点有变动，受影响的数据仅仅是此服务器到其环空间中前一台服务器（即沿着逆时针方向行走遇到的第一台服务器）之间数据，其它不会受到影响
 * <p>
 * 另外，一致性哈希算法在服务节点太少时，容易因为节点分部不均匀而造成数据倾斜问题。
 * 一致性哈希算法引入了虚拟节点机制，即对每一个服务节点计算多个哈希，每个计算结果位置都放置一个此服务节点，称为虚拟节点。具体做法可以在服务器Node后面增加编号来实现。
 * 
 * @author admin
 *
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

	private final ConcurrentMap<String, ConsistentHashSelector> selectorCache = new ConcurrentHashMap<String, ConsistentHashSelector>(); // 缓存相同服务及API的loadBalance选择器

	public ServiceInvoker doSelect(List<ServiceInvoker> invokerList, Object[] parameters) { // 同一次的loadBalance的多个invokers除服务提供者不一样，其他的应该是相同的
		ServiceInvoker serviceInvoker = invokerList.get(0);
		int identityHashCode = System.identityHashCode(invokerList);
		String key = serviceInvoker.getKey();
		ConsistentHashSelector selector = (ConsistentHashSelector) selectorCache.get(key);
		if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
			 selector = ConsistentHashSelector.newMurmurSelector(invokerList, identityHashCode);
			 //selector = ConsistentHashSelector.newketamaSelector(invokerList, identityHashCode);

			selectorCache.put(key, selector);
			selector = (ConsistentHashSelector) selectorCache.get(key);
		}
		return selector.select(parameters);
	}

}
