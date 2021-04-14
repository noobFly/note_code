package com.noob.LRU;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.google.common.base.Joiner;

/**
 * 参考 https://blog.csdn.net/elricboa/article/details/78847305
 * <p>
 * Least recently used，最近最长时间未被使用算法
 * <p>
 * 淘汰最长时间未被使用的数据，其核心思想是“如果数据最近被访问过，那么将来被访问的几率也更高”。
 * <p>
 * 较优的实现方式：
 * <p>
 * LinkedHashMap底层是用的HashMap加双链表实现的，而且有实现了按照访问顺序的存储。 定位数据时间复杂度基本上为O(1)
 * <p>
 * 操作过程大致如下：
 * <p>
 * 1. 新数据插入到链表尾部； 2. 每当缓存命中（即缓存数据被访问），则将数据移到链表尾部； 3. 当链表满的时候，将链表头部的数据丢弃。
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 873242808323499391L;
	private final int maxCapacity;
	private final Lock lock = new ReentrantLock();

	public LRUCache(int maxCapacity) {
		/**
		 * LinkedHashMap重写了hashMap的get操作：
		 * <p>
		 * accessOrder为true时(按访问排序), eg. 在get操作 -> getNode方法执行完后 再执行afterNodeAccess将获取到的Node移动到末尾; 
		 */
		super(maxCapacity, 0.75f, true);
		this.maxCapacity = maxCapacity;

	}

	/**
	 * hashMap在put(实际上是putVal)操作预留了扩展处理方法
	 * 
	 * <p>
	 * 首先：
	 * <p>
	 * 不存在指定KEY则创建新的节点。 LinkedHashMap重写了newNode方法, 会执行linkNodeLast方法将节点放到末尾
	 * <p>
	 * 存在指定KEY，则在获取Entry更新VALUE值后， 调用 afterNodeAccess 将获取到的Node移动到末尾；
	 * <p>
	 * 在最后执行afterNodeInsertion: 会执行removeEldestEntry方法判定是否需要删除最前面（最近最少访问的）节点
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > maxCapacity ; //因为是先put成功再判定是否需要删除，为避免超出容量则需设置为 size() >=  maxCapacity
	}

	private <T> T lock(Supplier<T> op) {
		try {
			lock.lock();
			return op.get();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return lock(() -> super.containsKey(key));
	}

	@Override
	public V get(Object key) {
		return lock(() -> super.get(key));

	}

	@Override
	public V put(K key, V value) {
		return lock(() -> super.put(key, value));

	}

	public int size() {
		return lock(() -> super.size());
	}

	public void clear() {
		try {
			lock.lock();
			super.clear();
		} finally {
			lock.unlock();
		}
	}

	public Set<Map.Entry<K, V>> getAll() {
		return lock(() -> super.entrySet());
	}

	/**
	 * 输出节点顺序
	 */
	public String linkNodeView() {
		return Joiner.on(",").join(this.keySet());
	}
}
