package com.noob.reids;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认的redis配置<br>
 * spring.redis.expires下的键值对作为缓存的“缓存名：缓存时间”的配置值<br>
 */
@ConfigurationProperties(prefix = "spring.cache", ignoreUnknownFields = true)
@ConditionalOnClass(value = org.springframework.data.redis.connection.RedisConnectionFactory.class)
@Configuration
public class DefaultRedisConfiguration {
    private Map<String, Long> expires;

    public Map<String, Long> getExpires() {
        return expires;
    }

    public void setExpires(Map<String, Long> expires) {
        this.expires = expires;
    }

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisSerializer<Object> redisSerializer = redisSerializer();
        SerializationPair<Object> jacksonSerializer = RedisSerializationContext.SerializationPair
                .fromSerializer(redisSerializer);
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(jacksonSerializer);

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        if (expires != null && !expires.isEmpty()) {
            expires.forEach((key, value) -> {
                RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(jacksonSerializer).entryTtl(Duration.ofSeconds(value)).prefixCacheNameWith(key);//建议不要用.disableKeyPrefix(); 毕竟redis是多业务共用的, 用带cache名的前缀来管理redis上的key!
                configMap.put(key, cacheConfig);
            });
        }

        return RedisCacheManager.builder(redisCacheWriter).cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(configMap).build();
    }
    
    private RedisSerializer<Object> redisSerializer() {
    	Jackson2JsonRedisSerializer<Object> redisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //java.time.LocalDateTime等序列化问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        
        redisSerializer.setObjectMapper(om);
        
        return redisSerializer;
    }
    
    @Bean(name = "redisTemplate")
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
 
        RedisSerializer<Object> valueRedisSerializer = redisSerializer();
        RedisSerializer<String> keyRedisSerializer = new StringRedisSerializer();
        
        redisTemplate.setKeySerializer(keyRedisSerializer);
        redisTemplate.setHashKeySerializer(keyRedisSerializer);
        redisTemplate.setValueSerializer(valueRedisSerializer);
        redisTemplate.setHashValueSerializer(valueRedisSerializer);
 
        return redisTemplate;
    }
}
