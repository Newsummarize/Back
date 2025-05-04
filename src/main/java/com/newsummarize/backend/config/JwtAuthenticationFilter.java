// Spring Security의 OncePerRequestFilter를 상속받아 한 요청 당 한 번만 실행되는 필터 정의
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

// 생성자 주입을 자동으로 생성해주는 Lombok 어노테이션
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 유효성 검증 및 사용자 정보 추출을 담당하는 컴포넌트
    private final JwtTokenProvider jwtTokenProvider;

    // 사용자 정보를 조회하기 위한 리포지토리
    private final UserRepository userRepository;

    // 로그아웃된 토큰을 저장하고 확인하는 데 사용하는 Redis
    private final RedisTemplate<String, String> redisTemplate;

    // 필터를 적용하지 않을 예외 URL 목록 정의
    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/users", "/api/users/",
            "/api/users/login", "/api/users/login/",
            "/api/users/logout", "/api/users/logout",
            "/api/news/category", "/api/news/category/",
            "/api/news/main", "/api/news/main/"
    );

    // 요청 경로가 예외 목록에 포함되면 필터를 건너뛰도록 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("🛡️ 필터 예외 검사 경로: " + path);
        return EXCLUDE_URLS.contains(path);
    }

    // 실제 필터 동작 정의
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        System.out.println("✅ JwtAuthenticationFilter 진입");

        // 요청 헤더에서 Authorization 값을 추출
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        // Authorization 헤더가 있고 Bearer 토큰 형식일 때만 처리
        if (header != null && header.startsWith("Bearer ")) {
            // "Bearer " 이후의 실제 토큰 부분만 추출
            String token = header.substring(7);
            System.out.println("🔐 Bearer 토큰 감지됨");

            // Redis에 해당 토큰이 로그아웃된 토큰으로 저장되어 있는지 확인
            if (redisTemplate.hasKey("logout:" + token)) {
                System.out.println("🚫 로그아웃된 토큰입니다.");
                chain.doFilter(req, res); // 다음 필터로 요청을 전달하고 종료
                return;
            }

            // 토큰이 유효한 경우
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 이메일(사용자 식별자) 추출
                String email = jwtTokenProvider.getUsername(token);
                System.out.println("✅ 토큰 유효, 사용자: " + email);

                // 이메일을 기준으로 사용자를 조회 (관심사 포함)
                User user = userRepository.findWithInterestsByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자 없음"));

                // Spring Security 인증 객체 생성 (비밀번호는 null, 권한 리스트는 비워둠)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, List.of());

                // 인증 세부 정보 설정 (IP, 세션 정보 등)
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // 인증 정보를 SecurityContext에 설정하여 이후 요청에서 인증된 사용자로 인식됨
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                System.out.println("❌ 토큰 유효성 실패");
            }
        } else {
            System.out.println("❌ Authorization 헤더 없음 또는 Bearer 아님");
        }

        // 다음 필터로 요청 전달
        chain.doFilter(req, res);
    }
}
