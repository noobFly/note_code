package com.noob2.threadPoolCustomize;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 要注意一些链接客户端复用后是否能有效处理流。FTPClient需要completePendingCommand
 *
 * @param <C> 客户端
 * @param <F> 对象池工厂
 */
@Slf4j
public abstract class AbstractClientManager<C, F extends BasePooledObjectFactory<C>> {
	public static interface UploadServerType {
		String FTPS = "ftps";
		String FTP = "ftp";
		String SFTP = "sftp";
		String OSS = "oss"; // 未支持
		String TANGO = "tango"; // 未支持
	}

	protected static int maxWaitingSignalTime = 5000;

	@Autowired
	private UploadServerService uploadServerService;
	
	@Value("${spring.profiles.active}")
	private String profile;

	private final ConcurrentHashMap<String, ObjectPool<C>> clientPoolMapping = new ConcurrentHashMap<>();
	// 连接池挂起信号灯
	private final ConcurrentHashMap<String, Boolean> onHoldSignal = new ConcurrentHashMap<>();

	private Class<F> factoryClass;

	@SuppressWarnings("unchecked")
	public AbstractClientManager() {
		ParameterizedType abstractClientManagerType = (ParameterizedType) this.getClass().getGenericSuperclass();
		Type basePooledObjectFactoryType = abstractClientManagerType.getActualTypeArguments()[1];

		if (basePooledObjectFactoryType instanceof Class<?>) {
			factoryClass = (Class<F>) basePooledObjectFactoryType;
		} else {
			ParameterizedType factoryType = (ParameterizedType) basePooledObjectFactoryType;
			factoryClass = (Class<F>) factoryType.getRawType();
		}
	}

	@PreDestroy
	protected void destory() { // springboot 容器销毁时关闭连接池
		clientPoolMapping.forEach((serverCode, pool) -> {
			try {
				pool.close();
			} catch (Exception e) {
				log.info("关闭连接池异常：" + serverCode, e);
			}
		});
	}

	@PostConstruct
	protected void init() { // 启动时初始化连接池
		String serverType = getServerType();

		Assert.hasLength(serverType, "serverType不能为空");

		List<UploadServer> ftpsServerList = uploadServerService.getEnableAll();

		ftpsServerList = ftpsServerList.stream().filter(item -> {
			String[] array = StringUtils.tokenizeToStringArray(item.getProfile(),
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			for (String temp : array) {
				if ("all".equals(temp)) {
					return true;
				}
				if (profile.equals(temp)) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());

		ftpsServerList.forEach(server -> {
			onHoldSignal.put(server.getServerCode(), true); // 设置挂起
		});

		ftpsServerList.forEach(server -> {
			initPool(server);
		});
	}

	public ObjectPool<C> getClientPool(String serverCode) {
		assetSignal(serverCode);

		ObjectPool<C> clientPool = clientPoolMapping.get(serverCode);
		if (clientPool == null) {
			throw new RuntimeException("未配置服务器编号为：" + serverCode + "的连接池");
		}

		return clientPool;
	}

	// 使用
	public void consume(String serverCode, Consumer<C> dealFunc) throws Exception {
		ObjectPool<C> clientPool = this.getClientPool(serverCode);
		C client = clientPool.borrowObject();
		try {
			dealFunc.accept(client);
		} finally {
			clientPool.returnObject(client);
		}
	}

	public void consumeDefault(Consumer<C> dealFunc) throws Exception {
		consume(getDefaultServerCode(), dealFunc);
	}

	public <R> R consume(String serverCode, Function<C, R> dealFunc) throws Exception {
		ObjectPool<C> clientPool = this.getClientPool(serverCode);
		C client = clientPool.borrowObject();
		try {
			return dealFunc.apply(client);
		} finally {
			clientPool.returnObject(client);
		}
	}

	public <R> R consumeDefault(Function<C, R> dealFunc) throws Exception {
		return consume(getDefaultServerCode(), dealFunc);
	}

	private void assetSignal(String serverCode) {
		long curTime = System.currentTimeMillis();
		while (onHoldSignal.getOrDefault(serverCode, false)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (System.currentTimeMillis() - curTime >= maxWaitingSignalTime) {
				throw new RuntimeException(String.format("等待%s连接池挂起信号超时，超时时间：%s", serverCode, maxWaitingSignalTime));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void initPool(UploadServer server) {
		F factory = (F) ReflectUtils.newInstance(factoryClass,
				new Class[] { String.class, int.class, String.class, String.class, Map.class },
				new Object[] { server.getHost(), server.getPort(), server.getUsername(), server.getPassword(),
						server.getExtJson() });

		int minActive = server.getMinActive() == null || server.getMinActive() < 1 ? 2 : server.getMinActive();
		int maxActive = server.getMaxActive() == null || server.getMaxActive() < 1 ? 10 : server.getMaxActive();

		GenericObjectPool<C> objectPool = new GenericObjectPool<C>(factory);
		objectPool.setMinIdle(minActive);
		objectPool.setMaxTotal(maxActive);
		objectPool.setMaxIdle(maxActive);
		objectPool.setMaxWaitMillis(5000); // 获取连接等待时间
		objectPool.setTestOnBorrow(true); // 借用的时间检查连接有效性
		objectPool.setTestWhileIdle(true);
		objectPool.setTestOnReturn(true);

		AbandonedConfig abandonedConfig = new AbandonedConfig();
		abandonedConfig.setRemoveAbandonedOnBorrow(true); // borrow 的时候检查泄漏
		abandonedConfig.setRemoveAbandonedTimeout(30); // 单位秒，借用未归还时间超时，因为是泄露了

		objectPool.setAbandonedConfig(abandonedConfig);
		objectPool.setTimeBetweenEvictionRunsMillis(15000); // 维护任务周期

		clientPoolMapping.put(server.getServerCode(), objectPool);
		onHoldSignal.put(server.getServerCode(), false);

		log.info("初始化{}连接池--{}--完成", server.getServerCode(), server.getServerName());
	}

	/**
	 * 重新加载服务连接池
	 * 
	 * @param serverCode
	 */
	public void reloadServer(String serverCode) {
		if (onHoldSignal.getOrDefault(serverCode, false)) {
			return;
		}

		onHoldSignal.put(serverCode, true); // 设置挂起

		try {
			UploadServer server = uploadServerService.findByServerCode(serverCode)
					.orElseThrow(() -> new RuntimeException(String.format("无编号为%s的upload_server配置信息", serverCode)));

			if (clientPoolMapping.containsKey(serverCode)) {
				ObjectPool<C> objectPool = clientPoolMapping.get(serverCode);
				objectPool.close(); // 关闭旧的连接池
				clientPoolMapping.remove(serverCode);
			}

			initPool(server);
		} catch (Exception e) {
			onHoldSignal.put(serverCode, false);
			throw e;
		}
	}

	/**
	 * 加载服务连接池
	 * 
	 * @param serverCode
	 */
	public void loadServer(String serverCode) {
		reloadServer(serverCode);
	}

	public void loadNew() {
		List<UploadServer> ftpsServerList = uploadServerService.getEnableAll();

		ftpsServerList.stream().filter(t -> !clientPoolMapping.containsKey(t.getServerCode()))
				.forEach(server -> initPool(server));
	}

	public abstract String getServerType();

	protected abstract String getDefaultServerCode();

}
