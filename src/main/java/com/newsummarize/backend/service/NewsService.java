// 뉴스 관련 비즈니스 로직을 처리하는 서비스 클래스
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
        String flaskUrl = "http://3.34.224.162:5001/news?category=all&limit=8";
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Flask 서버에서 뉴스 크롤링 결과 받아오기
            Map<String, Object> response = restTemplate.getForObject(flaskUrl, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            // 기존에 저장된 뉴스 URL 가져와서 중복 제거
            Set<String> existingUrls = newsRepository.findAllUrls();

            // 새로운 뉴스만 필터링해서 엔티티로 변환
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
                    .filter(news -> !existingUrls.contains(news.getUrl()))
                    .toList();

            // DB에 새로운 뉴스 저장
            newsRepository.saveAll(newNewsList);

            // 응답용 뉴스 객체 리스트 생성
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
            // 예외 발생 시 빈 리스트 반환
            System.out.println("❌ 실시간 주요 뉴스 크롤링 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // 사용자 관심사 기반 뉴스 추천
    public List<News> getRecommendedNews(String token) {
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 사용자 관심사 목록 추출
        List<String> allInterests = user.getInterests().stream()
                .map(Interest::getInterestCategory)
                .collect(Collectors.toList());

        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executor = Executors.newFixedThreadPool(6);

        try {
            // 관심사 셔플 후 2개 랜덤 선택
            List<String> shuffledInterests = new ArrayList<>(allInterests);
            Collections.shuffle(shuffledInterests);
            List<String> initialSelected = shuffledInterests.stream().limit(2).toList();

            // 병렬 비동기 요청
            List<CompletableFuture<News>> initialFutures = initialSelected.stream()
                    .map(category -> CompletableFuture.supplyAsync(() -> fetchNewsByCategory(category, restTemplate), executor))
                    .toList();

            // 결과 대기
            CompletableFuture.allOf(initialFutures.toArray(new CompletableFuture[0])).join();

            // 유효한 뉴스만 수집
            List<News> recommended = initialFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 결과 부족 시 다른 관심사로 보충 시도
            if (recommended.size() < 2) {
                for (String category : shuffledInterests) {
                    if (initialSelected.contains(category)) continue;

                    News news = fetchNewsByCategory(category, restTemplate);
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

    // 기본 카테고리(정치, 경제 등) 여부 확인
    private boolean isDefaultCategory(String category) {
        return Set.of("정치", "경제", "사회", "생활/문화", "세계", "IT/과학")
                .contains(category);
    }

    // 특정 카테고리에 대한 뉴스 1개 조회 (Flask 호출)
    private News fetchNewsByCategory(String category, RestTemplate restTemplate) {
        try {
            if (isDefaultCategory(category)) {
                String url = "http://3.34.224.162:5001/news?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
                return (articles != null && !articles.isEmpty()) ? mapToNews(articles.get(0)) : null;
            } else {
                Object rawResponse = searchByKeyword(category);
                if (rawResponse instanceof Map<?, ?> responseMap) {
                    List<Map<String, Object>> articles = (List<Map<String, Object>>) responseMap.get("articles");
                    return (articles != null && !articles.isEmpty()) ? mapToNews(articles.get(0)) : null;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 관심사 '" + category + "' 뉴스 추천 실패: " + e.getMessage());
        }
        return null;
    }

    // 기사 JSON → News 객체 변환
    private News mapToNews(Map<String, Object> article) {
        News news = new News();
        news.setTitle((String) article.get("title"));
        news.setPublisher((String) article.get("publisher"));
        news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
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

    // 특정 카테고리의 뉴스 10개 조회 (Flask 호출)
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
                news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
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

    // 키워드 기반 검색 (Flask 서버 호출)
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
