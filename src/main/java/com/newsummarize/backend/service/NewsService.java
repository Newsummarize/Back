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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public List<News> getMainNews() {
        String flaskUrl = "http://3.34.224.162:5001/news?category=all&limit=8";
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            Set<String> existingUrls = newsRepository.findAllUrls();

            List<News> newNewsList = articles.stream()
                    .map(article -> {
                        News news = new News();
                        news.setTitle((String) article.get("title"));
                        news.setPublisher((String) article.get("publisher"));
                        news.setPublished_at(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                        news.setUrl((String) article.get("url"));
                        news.setCategory((String) article.get("category"));
                        news.setImageUrl((String) article.get("image_url"));
                        news.setContent((String) article.get("content"));
                        return news;
                    })
                    .filter(news -> !existingUrls.contains(news.getUrl()))
                    .toList();

            newsRepository.saveAll(newNewsList);

            return articles.stream().map(article -> {
                News news = new News();
                news.setTitle((String) article.get("title"));
                news.setPublisher((String) article.get("publisher"));
                news.setPublished_at(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
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

        List<String> interests = user.getInterests().stream()
                .map(Interest::getInterestCategory)
                .collect(Collectors.toList());

        Set<String> defaultCategories = Set.of("정치", "경제", "사회", "생활/문화", "세계", "IT/과학");
        Set<String> allowedKeywords = Set.of(
                "정치", "경제", "사회", "생활/문화", "세계", "IT/과학",
                "스포츠", "주식", "부동산", "AI", "로봇", "교육",
                "연예", "영화", "드라마", "음악", "여행", "음식/맛집",
                "기후위기", "환경오염", "사건사고", "법원/검찰", "북한",
                "외교", "입시", "라이프스타일", "청소년", "철학"
        );

        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executor = Executors.newFixedThreadPool(6);

        List<CompletableFuture<News>> futures = interests.stream()
                .filter(allowedKeywords::contains)
                .map(category -> CompletableFuture.supplyAsync(() -> {
                    try {
                        if (defaultCategories.contains(category)) {
                            String url = "http://3.34.224.162:5001/news?category=" + category + "&limit=1";
                            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
                            return articles != null && !articles.isEmpty() ? mapToNews(articles.get(0)) : null;
                        } else {
                            Object rawResponse = searchByKeyword(category);
                            if (rawResponse instanceof Map<?, ?> responseMap) {
                                List<Map<String, Object>> articles = (List<Map<String, Object>>) responseMap.get("articles");
                                return articles != null && !articles.isEmpty() ? mapToNews(articles.get(0)) : null;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("❌ 관심사 '" + category + "' 뉴스 추천 실패: " + e.getMessage());
                    }
                    return null;
                }, executor))
                .toList();

        List<News> recommended = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        executor.shutdown();
        return recommended;
    }

    private News mapToNews(Map<String, Object> article) {
        News news = new News();
        news.setTitle((String) article.get("title"));
        news.setPublisher((String) article.get("publisher"));
        news.setPublished_at(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
        news.setUrl((String) article.get("url"));
        news.setCategory((String) article.get("category"));
        news.setImageUrl((String) article.get("image_url"));
        news.setContent((String) article.get("content"));

        Object idObj = article.get("news_id");
        if (idObj != null) {
            try {
                news.setId(((Number) idObj).longValue());
            } catch (Exception e) {
                System.out.println("⚠️ news_id 파싱 오류: " + idObj);
            }
        }
        return news;
    }

    public List<News> getNewsByCategory(String category) {
        String flaskUrl = "http://3.34.224.162:5001/news?category=" + category + "&limit=10";
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            return articles.stream().map(article -> {
                News news = new News();
                news.setTitle((String) article.get("title"));
                news.setPublisher((String) article.get("publisher"));
                news.setPublished_at(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                news.setUrl((String) article.get("url"));
                news.setCategory((String) article.get("category"));
                news.setImageUrl((String) article.get("image_url"));
                news.setContent((String) article.get("content"));
                news.setId(((Number) article.get("news_id")).longValue());
                return news;
            }).toList();

        } catch (Exception e) {
            System.out.println("❌ 카테고리 뉴스 크롤링 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Object searchByKeyword(String keyword) {
        try {
            String flaskUrl = "http://3.34.224.162:5009/api/news/recommend?keyword=" + keyword;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.getForEntity(flaskUrl, Object.class);
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Flask 서버 요청 실패: " + e.getMessage());
        }
    }
}