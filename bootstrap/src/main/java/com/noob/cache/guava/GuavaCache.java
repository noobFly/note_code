package com.noob.cache.guava;

import com.google.common.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 基于guava cache的spring cache适配
 *
 */
public class GuavaCache extends AbstractValueAdaptingCache {
	private final Cache<Object, Object> cache;
	private final String cacheName;

	protected GuavaCache(String cacheName, Cache<Object, Object> cache) {
		super(true);
		this.cacheName = cacheName;
		this.cache = cache;
	}

	@Override
	public String getName() {
		return cacheName;
	}

	@Override
	public Object getNativeCache() {
		return cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		try {
			return (T) cache.get(key, valueLoader);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void put(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		Object present = cache.getIfPresent(key);
		if(present == null) { //这里的判定及设置会有并发影响
			cache.put(key, value);
		}
		return toValueWrapper(present);
	}

	@Override
	public void evict(Object key) {
		cache.invalidate(key);
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	@Override
	protected Object lookup(Object key) {
		return cache.getIfPresent(key);
	}


}
