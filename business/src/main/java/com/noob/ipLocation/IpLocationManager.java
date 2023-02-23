package com.noob.ipLocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.noob.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * IP所在地管理器
 */
@Slf4j
public class IpLocationManager {
    private List<IpLocationQuerier> queriers = new ArrayList<>();
    private final LoadingCache<String, String> CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).maximumSize(5000).build(new CacheLoader<String, String>() {
        @Override
        public String load(String key) throws Exception {
            return _getLocation(key);
        }
    });

    public IpLocationManager() {
        queriers.add(new PconlineIpLocationQuerier()); //顺序即是优先级
        queriers.add(new Wy126IpLocationQuerier());
    }

    private String _getLocation(String ipAddr) {
        if (IpUtils.internalIp(ipAddr)) {
            return "内部IP";
        }

        for (IpLocationQuerier querier : queriers) {
            String[] location = querier.get(ipAddr);
            if (location != null) {
                return location[0] + location[1];
            }
        }
        return "未知";
    }

    public String getLocation(String ipAddr) {
        try {
            return CACHE.get(ipAddr);
        } catch (ExecutionException e) {
            log.error("获取IP所在地异常", e);
        }
        return "未知";
    }

    public static void main(String[] args) {
        IpLocationManager manager = new IpLocationManager();
        System.out.println(manager.getLocation("59.41.158.194"));
    }
}
