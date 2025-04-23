package com.newsummarize.backend.service;

import com.newsummarize.backend.config.JwtTokenProvider;
import com.newsummarize.backend.domain.Interest;
import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.repository.NewsRepository;
import com.newsummarize.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public List<News> getMainNews() {
        String flaskUrl = "http://3.37.61.85:5001/news?category=all&limit=8";
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            // ✅ 이미 DB에 저장된 뉴스 URL 목록 불러오기
            Set<String> existingUrls = newsRepository.findAllUrls();

            // ✅ 새로 들어온 뉴스 중 중복 아닌 것만 골라서 News 객체로 변환
            List<News> newNewsList = articles.stream()
                    .map(article -> {
                        News news = new News();
                        news.setTitle((String) article.get("title"));
                        news.setPublisher((String) article.get("publisher"));
                        news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                        news.setUrl((String) article.get("url"));
                        news.setCategory((String) article.get("category"));
                        news.setImageUrl((String) article.get("image_url"));
                        news.setContent((String) article.get("content"));
                        return news;
                    })
                    .filter(news -> !existingUrls.contains(news.getUrl())) // 중복 제거
                    .toList();

            // DB에 저장
            newsRepository.saveAll(newNewsList);

            // 최종적으로 응답할 뉴스 목록 (방금 받은 8개 전체 반환)
            return articles.stream().map(article -> {
                News news = new News();
                news.setTitle((String) article.get("title"));
                news.setPublisher((String) article.get("publisher"));
                news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                news.setUrl((String) article.get("url"));
                news.setCategory((String) article.get("category"));
                news.setImageUrl((String) article.get("image_url"));
                news.setContent((String) article.get("content"));
                news.setId(((Number) article.get("news_id")).longValue());
                return news;
            }).toList();

        } catch (Exception e) {
            System.out.println("❌ 실시간 주요 뉴스 크롤링 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public List<News> getRecommendedNews(String token) {
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String category = user.getInterests().stream()
                .findFirst()
                .map(Interest::getInterestCategory)
                .orElse("all");

        String flaskUrl = "http://3.37.61.85:5001/news?category=" + category + "&limit=2";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
        List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

        List<Map<String, Object>> top2Articles = articles.stream()
                .limit(2)
                .collect(Collectors.toList());

        List<News> result = top2Articles.stream().map(article -> {
            News news = new News();

            // id 수동 매핑
            Object idObj = article.get("news_id");
            if (idObj != null) {
                try {
                    news.setId(Long.parseLong(idObj.toString()));
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ news_id 파싱 오류: " + idObj);
                }
            }

            news.setTitle((String) article.get("title"));
            news.setPublisher((String) article.get("publisher"));

            String publishedAtStr = ((String) article.get("published_at")).replace(" ", "T");
            news.setPublishedAt(LocalDateTime.parse(publishedAtStr));

            news.setUrl((String) article.get("url"));
            news.setCategory((String) article.get("category"));
            news.setImageUrl((String) article.get("image_url"));
            news.setContent((String) article.get("content"));

            return news;
        }).toList();



        return result;
    }

    public List<News> getNewsByCategory(String category) {
        String flaskUrl = "http://3.37.61.85:5001/news?category=" + category + "&limit=10";
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            return articles.stream().map(article -> {
                News news = new News();
                news.setTitle((String) article.get("title"));
                news.setPublisher((String) article.get("publisher"));
                news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                news.setUrl((String) article.get("url"));
                news.setCategory((String) article.get("category"));
                news.setImageUrl((String) article.get("image_url"));
                news.setContent((String) article.get("content"));
                news.setId(((Number) article.get("news_id")).longValue()); // ID도 담아줌
                return news;
            }).toList();

        } catch (Exception e) {
            System.out.println("❌ 카테고리 뉴스 크롤링 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Object searchByKeyword(String keyword) {
        try {
            String flaskUrl = "http://3.37.61.85:5006/search?query=" + keyword;


            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.getForEntity(flaskUrl, Object.class);
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Flask 서버 요청 실패: " + e.getMessage());
        }
    }
}