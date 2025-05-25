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
    private static final List<String> EXCLUDE_PATH_PREFIXES = List.of(
            "/api/users",  // POST: 회원가입
            "/api/users/login",
            "/api/users/logout",
            "/api/news/category",
            "/api/news/main",
            "/api/search",
            "/api/search/analytics",
            "/api/search/analytics_num",
            "/api/search/timeline"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // POST /api/users만 필터 제외
        if (path.equals("/api/users") && "POST".equalsIgnoreCase(method)) return true;

        return EXCLUDE_PATH_PREFIXES.stream().anyMatch(path::startsWith);
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
                        .orElseThrow(() -> new RuntimeException("사용자 없음"));

                // 권한 부여: ROLE_USER
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
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
