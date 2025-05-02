package com.newsummarize.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 API URL 허용
                        .allowedOrigins(
                                "http://localhost:5173", // 로컬 개발용
                                "https://newsummarize.com" // 실제 배포용 도메인
                        )
                        // 허용할 프론트엔드 주소
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                        .allowedHeaders("*") // 모든 헤더 허용
                        .allowCredentials(true) // 쿠키/헤더 포함 허용 (JWT 인증할 때 필요할 수 있음)
                        .maxAge(3600); // preflight 요청 결과 1시간(3600초) 캐시
            }
        };
    }
}
