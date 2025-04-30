package com.newsummarize.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return "✅ Newsummarize 백엔드 서버입니다. Swagger는 /swagger-ui/index.html 에서 확인하세요.";
    }
}
