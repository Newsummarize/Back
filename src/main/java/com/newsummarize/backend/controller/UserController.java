package com.newsummarize.backend.controller;

import com.newsummarize.backend.config.JwtTokenProvider;
import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.dto.InterestRequest;
import com.newsummarize.backend.dto.MyPageResponse;
import com.newsummarize.backend.service.AuthService;
import com.newsummarize.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.newsummarize.backend.dto.ChangePasswordRequest;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    record MessageResponse(String message) {
    }

    @GetMapping("/me")
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyPage(user));
    }

    @PostMapping("/interests")
    public ResponseEntity<?> addInterest(@RequestBody InterestRequest request,
                                         @RequestHeader("Authorization") String authHeader) {
        String token = resolveToken(authHeader);
        userService.addInterest(token, request.getInterest());
        return ResponseEntity.ok().body(
                new MessageResponse("관심사 '" + request.getInterest() + "'가 추가되었습니다.")
        );
    }

    @DeleteMapping("/interests")
    public ResponseEntity<?> removeInterest(@RequestParam String interest,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = resolveToken(authHeader);
        userService.removeInterest(token, interest);
        return ResponseEntity.ok().body(
                new MessageResponse("관심사 '" + interest + "'가 삭제되었습니다.")
        );
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = resolveToken(authHeader);
        userService.changePassword(token, request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 성공적으로 변경되었습니다."));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        authService.logout(token);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        userService.deleteUser(token);
        return ResponseEntity.ok(new MessageResponse("회원 탈퇴가 완료되었습니다."));
    }

    private String resolveToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        throw new RuntimeException("유효하지 않은 Authorization 헤더입니다.");
    }

}