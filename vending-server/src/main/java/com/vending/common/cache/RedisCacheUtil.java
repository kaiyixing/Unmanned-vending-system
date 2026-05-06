package com.vending.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存 Key 前缀
    public static final String KEY_PRODUCT_LIST = "cache:product:list:";
    public static final String KEY_CABINET_LIST = "cache:cabinet:list:";
    public static final String KEY_CABINET_PRODUCTS = "cache:cabinet:products:";
    public static final String KEY_JWT_BLACKLIST = "jwt:blacklist:";
    public static final String KEY_REFRESH_TOKEN = "jwt:refresh:";

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除带前缀的所有缓存（使用 SCAN 替代 KEYS，分批删除避免 Redis 阻塞）
     */
    public void deleteByPrefix(String prefix) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(500)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
                if (keys.size() >= 100) {
                    redisTemplate.delete(keys);
                    keys.clear();
                }
            }
        }

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 判断 key 是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
