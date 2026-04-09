package com.beta.FindHome.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class RedisUtils {
    private static final Logger LOGGER = Logger.getLogger(RedisUtils.class.getName());
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisUtils(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean keyExists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if key exists in Redis", e);
            return false;
        }
    }

    public void save(String key, Object data) {
        try {
            if(!keyExists(key)){
                String json = objectMapper.writeValueAsString(data);
                redisTemplate.opsForValue().set(key, json);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving data to Redis", e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String jsonData = (String) redisTemplate.opsForValue().get(key);
            if (jsonData == null) {
                return null;
            }
            return objectMapper.readValue(jsonData, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.warning("Error deserializing JSON from Redis for key: {}"+ key+ e);
            return null;
        }
    }

    public boolean delete(String key) {
        try {
            if (keyExists(key)) {
                return Boolean.TRUE.equals(redisTemplate.delete(key));
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting key from Redis", e);
            return false;
        }
    }
}