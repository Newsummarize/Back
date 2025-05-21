package com.newsummarize.backend.service;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.dto.NumericalTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Set<String> VALID_PERIODS = Set.of("daily", "weekly", "monthly");

    public List<News> getSearchedNews(String keyword) {
        String flaskURL = "http://3.34.224.162:5006/search?keyword=" + keyword;
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(flaskURL, Map.class);
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");

            return articles.stream().map(article -> {
                News news = new News();
                news.setId(((Number) article.get("news_id")).longValue());
                news.setTitle((String) article.get("title"));
                news.setUrl((String) article.get("url"));
                news.setCategory((String) article.get("category"));
                news.setPublisher((String) article.get("publisher"));
                news.setPublishedAt(LocalDateTime.parse(((String) article.get("published_at")).replace(" ", "T")));
                news.setImageUrl((String) article.get("image_url"));
                news.setContent((String) article.get("content"));
                return news;
            }).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public byte[] getKeywordTrendImage(String keyword, String period) {
        if (!VALID_PERIODS.contains(period)) {
            throw new IllegalArgumentException("기간(period)은 {daily, weekly, monthly} 중 하나여야 합니다.");
        }
        String flaskURL = "http://3.34.224.162:5006/search/analytics?keyword=" + keyword + "&period=" + period;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(flaskURL, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("내부 서버 호출 실패: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("통신 중 에러 발생");
        }
    }

    public NumericalTrendResponse getNumericalAnalyticData(String keyword, String period) {

        if (!VALID_PERIODS.contains(period)) {
            throw new IllegalArgumentException("기간(period)은 {daily, weekly, monthly} 중 하나여야 합니다.");
        }

        String flaskURL = "http://localhost:5006/search/analytics_num?keyword=" + keyword + "&period=" + period;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<NumericalTrendResponse> response = restTemplate.exchange(
                    flaskURL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<NumericalTrendResponse>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("내부 서버 호출 실패: " + response.getStatusCode());
            }

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("통신 중 에러 발생");
        }
    }
}
