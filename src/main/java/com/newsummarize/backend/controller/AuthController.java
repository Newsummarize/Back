package com.newsummarize.backend.controller;

import com.newsummarize.backend.dto.LoginRequest;
import com.newsummarize.backend.dto.LoginResponse;
import com.newsummarize.backend.dto.SignupRequest;
import com.newsummarize.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        userService.signup(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        String token = userService.login(req);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
