// 인증 관련 로직을 담당하는 서비스 클래스 (주로 로그아웃 처리)
package com.newsummarize.backend.service;

import com.newsummarize.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// 스프링 서비스 컴포넌트임을 명시
@Service

// 생성자 주입을 위한 Lombok 어노테이션 (final 필드 자동 주입)
@RequiredArgsConstructor
public class AuthService {

    // RedisTemplate을 이용해 Redis에 로그아웃 토큰을 저장
    private final RedisTemplate<String, String> redisTemplate;

    // JWT 유틸리티 클래스 (토큰 파싱 및 만료시간 확인용)
    private final JwtTokenProvider jwtTokenProvider;

    // 로그아웃 처리 로직
    public void logout(String token) {
        // 토큰의 남은 유효 시간 (밀리초 단위)
        long expiration = jwtTokenProvider.getExpiration(token);

        // "logout:{토큰}" 키로 Redis에 저장하고, 만료 시간 설정
        // → 추후 필터에서 이 키 존재 여부로 로그아웃 여부 확인 가능
        redisTemplate.opsForValue().set("logout:" + token, "true", expiration, TimeUnit.MILLISECONDS);
    }
}
