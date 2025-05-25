package com.newsummarize.backend.service;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.dto.NumericalTrendResponse;
import com.newsummarize.backend.error.exception.InternalFlaskErrorException;
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

@Service
@RequiredArgsConstructor
public class SearchService {
    private final RestTemplate restTemplate;

    public List<News> getSearchedNews(String keyword) {
        String flaskURL = "http://127.0.0.1:5006/search?keyword=" + keyword;
        try {
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
        String flaskURL = "http://127.0.0.1:5006/search/analytics?keyword=" + keyword + "&period=" + period;
        ResponseEntity<byte[]> response = restTemplate.getForEntity(flaskURL, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful())
            throw new InternalFlaskErrorException("내부 서버 호출 실패: " + response.getStatusCode());

        return response.getBody();
    }

    public NumericalTrendResponse getNumericalAnalyticData(String keyword, String period) {
        String flaskURL = "http://127.0.0.1:5006/search/analytics_num?keyword=" + keyword + "&period=" + period;
        ResponseEntity<NumericalTrendResponse> response = restTemplate.exchange(
                flaskURL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<NumericalTrendResponse>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful())
            throw new InternalFlaskErrorException("내부 서버 호출 실패: " + response.getStatusCode());

        return response.getBody();
    }
}
