package com.newsummarize.backend.service;

import com.newsummarize.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);
        redisTemplate.opsForValue().set("logout:" + token, "true", expiration, TimeUnit.MILLISECONDS);
    }
}

