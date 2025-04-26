package com.newsummarize.backend.controller;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/main")
    public List<News> getMainNews() {
        return newsService.getMainNews();
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<News>> recommend(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        List<News> news = newsService.getRecommendedNews(token);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/category")
    public ResponseEntity<List<News>> getNewsByCategory(@RequestParam String category) {
        List<News> news = newsService.getNewsByCategory(category);
        return ResponseEntity.ok(news);
    }


}
