// 뉴스 관련 요청을 처리하는 컨트롤러 클래스
package com.newsummarize.backend.controller;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST API 컨트롤러임을 나타냄 (JSON 형식의 응답 처리)
@RestController

// 생성자를 통한 의존성 주입을 자동으로 처리 (Lombok 어노테이션)
@RequiredArgsConstructor

// 이 컨트롤러의 기본 URL 경로를 "/api/news"로 설정
@RequestMapping("/api/news")
public class NewsController {

    // 뉴스 관련 비즈니스 로직을 처리하는 서비스 클래스
    private final NewsService newsService;

    // [GET] /api/news/main
    // 주요 뉴스 목록을 조회하는 API
    @GetMapping("/main")
    public List<News> getMainNews() {
        // DB 또는 외부 크롤링을 통해 주요 뉴스를 가져와 반환
        return newsService.getMainNews();
    }

    // [GET] /api/news/recommend
    // JWT 인증 기반으로 사용자 맞춤 뉴스 추천을 수행하는 API
    @GetMapping("/recommend")
    public ResponseEntity<List<News>> recommend(@RequestHeader("Authorization") String authHeader) {
        // "Bearer {token}" 형식에서 "Bearer " 제거하고 실제 토큰만 추출
        String token = authHeader.replace("Bearer ", "");

        // 사용자 토큰을 기반으로 관심사에 맞는 뉴스 추천
        List<News> news = newsService.getRecommendedNews(token);

        // 추천된 뉴스 리스트를 200 OK 응답으로 반환
        return ResponseEntity.ok(news);
    }

    // [GET] /api/news/category?category=경제
    // 뉴스 카테고리(정치, 경제 등)를 기준으로 뉴스 리스트를 조회
    @GetMapping("/category")
    public ResponseEntity<List<News>> getNewsByCategory(@RequestParam String category) {
        // 파라미터로 전달된 카테고리 기준 뉴스 조회
        List<News> news = newsService.getNewsByCategory(category);

        // 해당 카테고리 뉴스 리스트를 응답으로 반환
        return ResponseEntity.ok(news);
    }
}
