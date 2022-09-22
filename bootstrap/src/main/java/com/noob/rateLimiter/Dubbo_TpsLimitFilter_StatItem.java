
package com.noob.rateLimiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * dubbo的TpsLimitFilter也是用的是固定窗口计数模式StatItem
 */
public class Dubbo_TpsLimitFilter_StatItem {

    private String name;

    private long lastResetTime; // 上次重置时间

    private long interval; // 窗口时间周期

    private AtomicInteger token; // 计数

    private int rate; // 速率

    Dubbo_TpsLimitFilter_StatItem(String name, int rate, long interval) {
        this.name = name;
        this.rate = rate;
        this.interval = interval;
        this.lastResetTime = System.currentTimeMillis();
        this.token = new AtomicInteger(rate);
    }

    public boolean isAllowable() {
        long now = System.currentTimeMillis();
        if (now > lastResetTime + interval) { // 当前时间超出了 上次重置时间lastResetTime + 时间单元interval , 则重新设置token和lastResetTime
            token.set(rate);
            lastResetTime = now;
        }

        int value = token.get();
        boolean flag = false;
        while (value > 0 && !flag) {
            flag = token.compareAndSet(value, value - 1); // while + CAS 来递减直到成功！ 如果value已经为0了还未成功则返回false
            value = token.get();
        }

        return flag;
    }

    long getLastResetTime() {
        return lastResetTime;
    }

    int getToken() {
        return token.get();
    }


}
