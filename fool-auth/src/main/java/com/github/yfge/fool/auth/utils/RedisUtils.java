package com.github.yfge.fool.auth.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public <T> T getObject(String key, Class<T> clazz) {
        String value = this.get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value, clazz);
            } catch (IOException e) {
                return null;
            }
        } else {

            return null;
        }
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set
                (key, value);
    }

    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set
                (key, value, timeout, TimeUnit.MILLISECONDS);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void setObject(String key, Object object) {
        String value = null;
        try {
            value = objectMapper.writeValueAsString(object);
            this.set(key, value);
        } catch (JsonProcessingException e) {

            return;
        }

    }

    public void del(String tokenKey) {
        redisTemplate.delete(tokenKey);
    }
}
