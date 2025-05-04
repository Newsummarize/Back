// 스프링 웹 MVC 설정을 위한 구성 클래스
package com.newsummarize.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 이 클래스가 스프링 설정 클래스임을 나타냄
@Configuration
public class WebConfig {

    // CORS 관련 설정을 적용할 WebMvcConfigurer Bean을 등록
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            // CORS 매핑 규칙 정의
            @Override
            public void addCorsMappings(CorsRegistry registry) {
<<<<<<< HEAD
                registry.addMapping("/**") // 모든 URL 경로에 대해 CORS 적용
                        .allowedOrigins(
                                "http://localhost:5173",        // 개발 환경 (로컬 프론트엔드 주소)
                                "https://newsummarize.com"     // 배포 환경 도메인
                        )
                        // 허용할 HTTP 요청 메서드 목록
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 모든 요청 헤더 허용
                        .allowedHeaders("*")
                        // 클라이언트에서 자격 증명(쿠키, Authorization 헤더 등)을 포함할 수 있도록 허용
                        .allowCredentials(true)
                        // preflight 요청 결과를 3600초(1시간) 동안 캐시 (OPTIONS 요청 최소화)
                        .maxAge(3600);
=======
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
>>>>>>> 6cbeea2fd0aec32005a071a6689ede579ebb0f75
            }
        };
    }
}
