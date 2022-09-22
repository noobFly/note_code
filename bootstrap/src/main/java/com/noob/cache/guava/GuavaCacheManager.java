package com.noob.cache.guava;

import com.google.common.cache.CacheBuilder;
import lombok.Data;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于guava cache的CacheManager实现
 *
 */
public class GuavaCacheManager implements CacheManager {
	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);
	private final Map<String, CacheConfig> cacheConfigMap = new HashMap<>();

	@Override
	public Cache getCache(String name) {
		return cacheMap.computeIfAbsent(name, key -> {
			CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
			CacheConfig cacheConfig = cacheConfigMap.get(key);
			if(cacheConfig != null && cacheConfig.getTtl() != null) {
				if(cacheConfig.getExpirePolicy() == ExpirePolicy.AFTER_WRITE) {
					builder.expireAfterWrite(cacheConfig.getTtl(), cacheConfig.getTtlUnit());
				} else {
					builder.expireAfterAccess(cacheConfig.getTtl(), cacheConfig.getTtlUnit());
				}
				if(cacheConfig.getMaximumSize() != null) {
					builder.maximumSize(cacheConfig.getMaximumSize());
				}
			}
			return new GuavaCache(name, builder.build());
		});
	}

	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableCollection(cacheMap.keySet());
	}
	
	public void addCacheConfig(CacheConfig config) {
		cacheConfigMap.put(config.getCacheName(), config);
	}

	public enum ExpirePolicy {
		/** 写后 */
		AFTER_WRITE, 
		/** 访问后 */
		AFTER_ACCESS
	}
	
	@Data
	public static class CacheConfig {
		/** 缓存名 */
		private String cacheName;
		/** 生命期 */
		private Long ttl;
		/** 生命期单位 */
		private TimeUnit ttlUnit = TimeUnit.SECONDS;
		/** 生效策略 */
		private ExpirePolicy expirePolicy = ExpirePolicy.AFTER_WRITE;
		/** 缓存元素最大数量 */
		private Long maximumSize;

		public CacheConfig(String cacheName, Long ttl, TimeUnit ttlUnit, ExpirePolicy expirePolicy, Long maximumSize) {
			super();
			this.cacheName = cacheName;
			this.ttl = ttl;
			this.ttlUnit = ttlUnit;
			this.expirePolicy = expirePolicy;
			this.maximumSize = maximumSize;
		}

		public CacheConfig(String cacheName, Long ttl) {
			super();
			this.cacheName = cacheName;
			this.ttl = ttl;
		}
		
	}
}
