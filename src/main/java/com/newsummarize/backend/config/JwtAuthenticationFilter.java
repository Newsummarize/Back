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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/users", "/api/users/",
            "/api/users/login", "/api/users/login/",
            "/api/users/logout", "/api/users/logout",
            "/api/news/category", "/api/news/category/",
            "/api/news/main", "/api/news/main/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("🛡️ 필터 예외 검사 경로: " + path);
        return EXCLUDE_URLS.contains(path);
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

            // ✅ Redis에서 로그아웃 여부 확인
            if (redisTemplate.hasKey("logout:" + token)) {
                System.out.println("🚫 로그아웃된 토큰입니다.");
                chain.doFilter(req, res);
                return;
            }

            // ✅ 토큰 유효성 검사
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getUsername(token);
                System.out.println("✅ 토큰 유효, 사용자: " + email);

                User user = userRepository.findWithInterestsByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자 없음"));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, List.of());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                System.out.println("❌ 토큰 유효성 실패");
            }
        } else {
            System.out.println("❌ Authorization 헤더 없음 또는 Bearer 아님");
        }

        chain.doFilter(req, res);
    }
}
