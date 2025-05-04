// Redis 설정을 위한 클래스임을 나타내는 어노테이션
package com.newsummarize.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

// 이 클래스가 설정 파일임을 나타냄 (스프링이 인식해서 Bean 등록)
@Configuration
// 애플리케이션에서 캐시 기능을 활성화
@EnableCaching
public class RedisConfig {

    // application.yml 또는 properties에서 설정된 redis 값들을 주입받음
    private final RedisProperties redisProperties;

    // 생성자를 통해 RedisProperties 주입 (host, port 등을 사용)
    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    // Redis와의 연결을 위한 ConnectionFactory Bean 등록
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 단일 Redis 서버 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost()); // 호스트 주소 설정
        config.setPort(redisProperties.getPort());     // 포트 설정
        config.setPassword(RedisPassword.of("password0419")); // Redis 인증 비밀번호 설정

        // Lettuce 라이브러리를 사용하여 Redis 연결 팩토리 생성
        return new LettuceConnectionFactory(config);
    }

    // RedisTemplate Bean 등록 – Redis에서 key/value를 처리하기 위한 템플릿
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Redis 연결 팩토리 설정
        template.setConnectionFactory(redisConnectionFactory);

        // key와 value 모두 문자열로 직렬화하여 저장
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
