// JWT 토큰 생성 및 검증을 처리하는 컴포넌트
package com.newsummarize.backend.config;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

// 이 클래스는 스프링 컴포넌트로 등록되어 의존성 주입이 가능하게 된다
@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 비밀 키 (base64 인코딩된 문자열)
    private String secretKey;

    // 토큰의 유효 시간 (밀리초 단위)
    private final long validityInMs;

    // JwtProperties를 주입받아 설정값을 초기화하는 생성자
    public JwtTokenProvider(JwtProperties jwtProperties) {
        // 필수 설정값이 누락되었는지 확인
        if (jwtProperties.getSecret() == null || jwtProperties.getExpiration() == null) {
            throw new IllegalStateException("JWT 설정 누락: application.yml에 secret/expiration 확인");
        }

        // 비밀 키와 만료 시간 설정
        this.secretKey = jwtProperties.getSecret();
        this.validityInMs = jwtProperties.getExpiration();
    }

    // 컴포넌트 초기화 이후 base64 인코딩된 키로 변환 (JWT 라이브러리에서 요구)
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // 사용자 이름(이메일 등)을 기반으로 JWT 토큰을 생성하는 메서드
    public String createToken(String username) {
        Date now = new Date(); // 현재 시간
        Date exp = new Date(now.getTime() + validityInMs); // 만료 시간 계산

        // JWT 생성: subject는 사용자 식별자, 서명 알고리즘은 HS256 사용
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 전달된 토큰이 유효한지 검증 (만료 여부, 위조 여부)
    public boolean validateToken(String token) {
        try {
            // 토큰을 파싱하면서 예외가 발생하지 않으면 유효함
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 위조, 파싱 실패, 만료 등의 경우 false 반환
            return false;
        }
    }

    // 토큰에서 사용자 이름(Subject)을 추출
    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰이 남은 유효 시간(ms)을 반환
    public long getExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime() - System.currentTimeMillis();
    }

    // HttpServletRequest에서 Authorization 헤더를 추출하고 Bearer 토큰 형식이면 토큰 반환
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Bearer 접두어 제거 후 순수 토큰 반환
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰에서 사용자 이메일을 추출하는 메서드 (Bearer 포함된 경우에도 대응)
    public String getUserEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody()
                .getSubject();
    }

}
