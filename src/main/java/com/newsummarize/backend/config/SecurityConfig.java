package com.newsummarize.backend.config;

import com.newsummarize.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        System.out.println("✅ JwtAuthenticationFilter Bean 생성됨");
        return new JwtAuthenticationFilter(jwtTokenProvider, userRepository, redisTemplate);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("✅ SecurityFilterChain 설정 시작");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 회원가입(POST /api/users)만 허용
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()



                        // 로그인, 로그아웃 등은 여전히 허용
                        .requestMatchers(
                                "/api/users/login", "/api/users/logout",
                                "/api/news/category", "/api/news/main",
                                "/api/search", "/api/search/analytics", "/api/search/analytics_num",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**"
                        ).permitAll()

                        // 프리플라이트 OPTIONS 요청 허용 (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트 도메인 등록 (로컬 + 배포)
        config.setAllowedOrigins(List.of("https://newsummarize.com", "http://localhost:5173"));

        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 요청 헤더 허용
        config.setAllowedHeaders(List.of("*"));

        // 클라이언트가 응답 헤더 중 Authorization 등을 읽을 수 있도록
        config.setExposedHeaders(List.of("Authorization"));

        // withCredentials: true 사용할 수 있도록 설정
        config.setAllowCredentials(true);

        // 위 설정을 모든 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
