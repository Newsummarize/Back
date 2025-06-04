// 사용자 정보 관련 API (마이페이지, 관심사, 비밀번호 등)를 처리하는 컨트롤러
package com.newsummarize.backend.controller;

import com.newsummarize.backend.config.JwtTokenProvider;
import com.newsummarize.backend.dto.MyPageResponse;
import com.newsummarize.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.newsummarize.backend.dto.ChangePasswordRequest;
import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.repository.UserRepository;



@CrossOrigin(origins = "https://newsummarize.com", allowedHeaders = "*")

// REST API 컨트롤러임을 명시
@RestController


// 생성자 주입을 위한 Lombok 어노테이션
@RequiredArgsConstructor

// 모든 요청의 기본 URL 경로는 /api/users
@RequestMapping("/api/users")
public class UserController {

    // 사용자 관련 서비스 (마이페이지, 관심사, 비밀번호 등)
    private final UserService userService;

    // JWT 토큰 관련 유틸리티 (파싱, 검증)
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 응답 메시지를 담기 위한 간단한 레코드 클래스
    record MessageResponse(String message) {
    }

    // [GET] /api/users/me
    // 현재 로그인한 사용자의 마이페이지 정보 조회
    @GetMapping("/my")
    public ResponseEntity<MyPageResponse> getMyPage() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("❌ 인증되지 않은 사용자");
            return ResponseEntity.status(401).build();
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            System.out.println("❌ Principal이 User 타입이 아님: " + principal);
            return ResponseEntity.status(401).build();
        }

        System.out.println("✅ 현재 사용자 이메일: " + user.getEmail());

        MyPageResponse response = userService.getMyPage(user);
        return ResponseEntity.ok(response);
    }












    // [POST] /api/users/interests
    // 관심사 추가 (토큰 기반 사용자 식별)
    @PostMapping("/interests")
    public ResponseEntity<?> addInterest(@RequestHeader("Authorization") String authHeader,
                                         @RequestParam("interest") String interestCategory) {
        String token = resolveToken(authHeader);
        userService.addInterest(token, interestCategory);
        return ResponseEntity.ok().body(
                new MessageResponse("관심사 '" + interestCategory + "'가 추가되었습니다.")
        );
    }

    // [DELETE] /api/users/interests
    // 관심사 제거 (토큰 기반 사용자 식별)
    @DeleteMapping("/interests")
    public ResponseEntity<?> removeInterest(@RequestParam String interest,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = resolveToken(authHeader);
        userService.removeInterest(token, interest);
        return ResponseEntity.ok().body(
                new MessageResponse("관심사 '" + interest + "'가 삭제되었습니다.")
        );
    }

    // [PUT] /api/users/password
    // 비밀번호 변경 요청 처리
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = resolveToken(authHeader);
        userService.changePassword(token, request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 성공적으로 변경되었습니다."));
    }

    // [DELETE] /api/users
    // 회원 탈퇴 처리
    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        userService.deleteUser(token);
        return ResponseEntity.ok(new MessageResponse("회원 탈퇴가 완료되었습니다."));
    }

    // Authorization 헤더에서 Bearer 토큰 추출 (접두어 제거)
    private String resolveToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        throw new RuntimeException("유효하지 않은 Authorization 헤더입니다.");
    }
}
