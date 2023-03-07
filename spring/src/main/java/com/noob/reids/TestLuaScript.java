package com.noob.reids;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * 用户交易密码管理
 */
public class TestLuaScript {


    private RedisScript<String> LOCK_SCRIPT = new DefaultRedisScript<String>(
            "if redis.call('EXISTS', KEYS[1]) == 0 then redis.call('SET', KEYS[1], 'true') end; redis.call('EXPIRE', KEYS[1], ARGV[1]); redis.call('DEL',KEYS[2]); return 'OK';");

    private RedisScript<String> COUNT_RESET_SCRIPT = new DefaultRedisScript<String>(
            "if redis.call('EXISTS', KEYS[1]) == 0 then redis.call('SET', KEYS[1], 0) end; redis.call('INCR', KEYS[1]); redis.call('EXPIRE',KEYS[1], ARGV[1]); return 'OK';");

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    private void init() {
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        redisConnection.scriptLoad(LOCK_SCRIPT.getScriptAsString().getBytes());
        redisConnection.scriptLoad(COUNT_RESET_SCRIPT.getScriptAsString().getBytes()); // 提前预先load到redis中。
    }


    private void checkPwd() {
        redisTemplate.execute(LOCK_SCRIPT, Arrays.asList("lockKey", "countKey"), 6 * 60 * 60);
        redisTemplate.execute(COUNT_RESET_SCRIPT, Arrays.asList("COUNT_SMS_RESET_TRADE_PWD_KEY"), 5 * 60);
    }


}
