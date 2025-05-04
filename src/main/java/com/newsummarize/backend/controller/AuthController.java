// 사용자 인증 관련 API를 담당하는 컨트롤러 클래스
package com.newsummarize.backend.controller;

import com.newsummarize.backend.dto.LoginRequest;
import com.newsummarize.backend.dto.LoginResponse;
import com.newsummarize.backend.dto.SignupRequest;
import com.newsummarize.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드는 JSON을 반환)
@RestController

// API 요청 경로를 "/api/users"로 매핑 (기본 경로)
@RequestMapping("/api/users")

// 생성자 기반 의존성 주입을 Lombok이 자동으로 생성해줌
@RequiredArgsConstructor
public class AuthController {

    // 회원가입 및 로그인 로직을 처리하는 서비스 레이어 주입
    private final UserService userService;

    // [POST] /api/users
    // 회원가입 요청 처리 메서드
    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        // 요청 본문에 담긴 사용자 정보를 기반으로 회원가입 수행
        userService.signup(req);

        // 응답은 200 OK, 바디 없음
        return ResponseEntity.ok().build();
    }

    // [POST] /api/users/login
    // 로그인 요청 처리 메서드
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        // 로그인 로직 수행 후 JWT 토큰 반환
        String token = userService.login(req);

        // 토큰을 LoginResponse DTO에 담아 반환
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
