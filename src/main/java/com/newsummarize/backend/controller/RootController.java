// 루트 경로(`/`) 요청에 응답하기 위한 컨트롤러
package com.newsummarize.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 해당 클래스가 REST API 컨트롤러임을 나타냄
@RestController
public class RootController {

    // [GET] /
    // 루트 경로 접근 시 간단한 서버 상태 메시지를 반환
    @GetMapping("/")
    public String home() {
        // 서버가 정상 작동 중임을 나타내는 문자열 응답 반환
        // Swagger UI의 접근 경로도 함께 안내함
        return "✅ Newsummarize 백엔드 서버입니다. Swagger는 /swagger-ui/index.html 에서 확인하세요.";
    }
}
