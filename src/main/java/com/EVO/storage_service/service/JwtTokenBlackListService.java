package com.EVO.storage_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenBlackListService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
