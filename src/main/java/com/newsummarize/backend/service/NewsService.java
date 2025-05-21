// 뉴스 관련 비즈니스 로직을 처리하는 서비스 클래스
package com.newsummarize.backend.service;

import com.newsummarize.backend.config.JwtTokenProvider;
import com.newsummarize.backend.domain.Interest;
import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.repository.NewsRepository;
import com.newsummarize.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 실시간 메인 뉴스 조회 및 저장

    @Transactional
    public List<News> getMainNews() {
        String flaskUrl = "http://3.34.224.162:5001/news?category=all&limit=20";
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            // 중복 방지를 위해 URL 기준으로 중복 제거
            Map<String, Map<String, Object>> uniqueArticles = new LinkedHashMap<>();
            for (Map<String, Object> article : articles) {
                String url = (String) article.get("url");
                if (!uniqueArticles.containsKey(url)) {
                    uniqueArticles.put(url, article);
                }
            }

            // DB에 이미 있는 URL 확인
            Set<String> existingUrls = newsRepository.findAllUrls();
            List<News> toSave = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : uniqueArticles.entrySet()) {
                String url = entry.getKey();
                if (!existingUrls.contains(url)) {
                    News news = mapToNews(entry.getValue());
                    toSave.add(news);
                }
            }

            // 저장
            newsRepository.saveAll(toSave);

            // 저장된 뉴스 중 랜덤으로 8개 가져오기
            return newsRepository.findRandom8News();

        } catch (Exception e) {
            System.out.println("❌ 실시간 주요 뉴스 크롤링 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }



    // 사용자 관심사 기반 뉴스 추천
    @Transactional
    public List<News> getRecommendation(User user) {
        List<String> allInterests = user.getInterests().stream()
                .map(Interest::getInterestCategory)
                .collect(Collectors.toList());

        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executor = Executors.newFixedThreadPool(6);

        try {
            List<String> shuffledInterests = new ArrayList<>(allInterests);
            Collections.shuffle(shuffledInterests);
            List<String> initialSelected = shuffledInterests.stream().limit(2).toList();

            List<CompletableFuture<News>> initialFutures = initialSelected.stream()
                    .map(category -> CompletableFuture.supplyAsync(() -> fetchAndSaveQuickNews(category, restTemplate), executor))
                    .toList();

            CompletableFuture.allOf(initialFutures.toArray(new CompletableFuture[0])).join();

            List<News> recommended = initialFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (recommended.size() < 2) {
                for (String category : shuffledInterests) {
                    if (initialSelected.contains(category)) continue;

                    News news = fetchAndSaveQuickNews(category, restTemplate);
                    if (news != null) {
                        recommended.add(news);
                        if (recommended.size() == 2) break;
                    }
                }
            }

            return recommended;

        } finally {
            executor.shutdown();
        }
    }

    // 빠르게 크롤링하고 DB에 저장까지 하는 함수
    @Transactional
    private News fetchAndSaveQuickNews(String category, RestTemplate restTemplate) {
        try {
            if (isDefaultCategory(category)) {
                String url = "http://3.34.224.162:5001/quick-news?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
                if (articles != null && !articles.isEmpty()) {
                    News news = mapToNews(articles.get(0));
                    newsRepository.save(news);
                    return news;
                }
            } else {
                Object rawResponse = searchByKeyword(category);
                if (rawResponse instanceof Map<?, ?> responseMap) {
                    List<Map<String, Object>> articles = (List<Map<String, Object>>) responseMap.get("articles");
                    if (articles != null && !articles.isEmpty()) {
                        News news = mapToNews(articles.get(0));
                        newsRepository.save(news);
                        return news;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 빠른 뉴스 저장 실패: " + e.getMessage());
        }
        return null;
    }

    // 기본 카테고리 여부 확인
    private boolean isDefaultCategory(String category) {
        return Set.of("정치", "경제", "사회", "생활/문화", "세계", "IT/과학").contains(category);
    }

    // JSON → News 객체 변환
    private News mapToNews(Map<String, Object> article) {
        News news = new News();
        news.setTitle((String) article.get("title"));
        news.setPublisher((String) article.get("publisher"));
        news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
        news.setUrl((String) article.get("url"));
        news.setCategory((String) article.get("category"));
        news.setImageUrl((String) article.get("image_url"));
        news.setContent((String) article.get("content"));
        return news;
    }

    // 키워드 뉴스 검색
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

    // 카테고리 뉴스 조회
    public List<News> getNewsByCategory(String category) {
        String flaskUrl = "http://3.34.224.162:5001/news?category=" + category + "&limit=10";
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
            return articles.stream().map(this::mapToNews).toList();
        } catch (Exception e) {
            System.out.println("❌ 카테고리 뉴스 조회 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
