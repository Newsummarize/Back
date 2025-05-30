package com.newsummarize.backend.config;

import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 필터를 적용하지 않을 경로 prefix 목록
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/api/users",              // POST 회원가입
            "/api/users/login",        // 로그인
            "/api/users/logout",
            "/api/news/category",
            "/api/news/main",
            "/api/search",
            "/api/search/analytics",
            "/api/search/analytics_num",
            "/api/search/timeline"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // 회원가입만 POST 허용
        if (path.equals("/api/users") && method.equalsIgnoreCase("POST")) return true;

        // 정확한 경로만 필터 제외
        return EXCLUDE_PATHS.contains(path);
    }



    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        System.out.println("✅ JwtAuthenticationFilter 진입");

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("🔐 Bearer 토큰 감지됨");

            if (redisTemplate.hasKey("logout:" + token)) {
                System.out.println("🚫 로그아웃된 토큰입니다.");
                chain.doFilter(req, res);
                return;
            }

            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getUsername(token);
                System.out.println("✅ 토큰 유효, 사용자: " + email);

                User user = userRepository.findWithInterestsByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

                // ✅ Spring Security 기본 User 객체 사용
                org.springframework.security.core.userdetails.User springUser =
                        new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                // 반드시 명시적으로 인증 상태로 설정!
                // auth.setAuthenticated(true);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("📌 인증 객체 설정 완료: " + auth);

            } else {
                System.out.println("❌ 토큰 유효성 실패");
            }
        } else {
            System.out.println("❌ Authorization 헤더 없음 또는 Bearer 아님");
        }

        chain.doFilter(req, res);
    }

}