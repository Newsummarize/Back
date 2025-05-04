// Spring Security 전반적인 설정을 담당하는 구성 클래스
package com.newsummarize.backend.config;

import com.newsummarize.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 해당 클래스가 스프링의 설정 클래스임을 명시
@Configuration
// 생성자를 통해 필요한 의존성을 자동으로 주입받도록 설정 (Lombok 제공)
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 관련 토큰 처리 기능 제공 클래스
    private final JwtTokenProvider jwtTokenProvider;

    // 사용자 정보를 조회하기 위한 JPA 리포지토리
    private final UserRepository userRepository;

    // RedisTemplate은 로그아웃 토큰 저장에 활용됨
    private final RedisTemplate<String, String> redisTemplate;

    // JwtAuthenticationFilter를 빈으로 등록 (DI 대상)
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        System.out.println("✅ JwtAuthenticationFilter Bean 생성됨");
        return new JwtAuthenticationFilter(jwtTokenProvider, userRepository, redisTemplate);
    }

    // Spring Security의 필터 체인을 정의하고 설정하는 메서드
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("✅ SecurityFilterChain 설정 시작");

        http
                // CSRF 보호 비활성화 (JWT 기반 API 서버에서는 일반적으로 비활성화)
                .csrf(csrf -> csrf.disable())

                // 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 아래 경로는 인증 없이 접근 허용 (회원가입, 로그인, 로그아웃, 뉴스 API, Swagger 등)
                        .requestMatchers(
                                "/",
                                "/api/users",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/news/category",
                                "/api/news/main",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 그 외의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // UsernamePasswordAuthenticationFilter 앞에 JWT 인증 필터를 삽입
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 구성된 보안 필터 체인을 반환
        return http.build();
    }

    // 비밀번호 암호화를 위한 PasswordEncoder Bean 등록 (BCrypt 알고리즘 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
