package com.newsummarize.backend.controller;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.dto.NumericalTrendResponse;
import com.newsummarize.backend.dto.TimelineResponse;
import com.newsummarize.backend.error.exception.InvalidRequestParameterException;
import com.newsummarize.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    private static final Set<String> VALID_PERIODS = Set.of("daily", "weekly", "monthly");

    @GetMapping("")
    public ResponseEntity<List<News>> getSearchedNews(@RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty())
            throw new InvalidRequestParameterException("검색어(keyword)가 필요합니다.");

        List<News> news = searchService.getSearchedNews(keyword);
        return ResponseEntity.ok().body(news);
    }

    @GetMapping("/analytics")
    public ResponseEntity<byte[]> getKeywordTrendImage(@RequestParam String keyword, @RequestParam String period) {

        if (keyword == null || keyword.trim().isEmpty())
            throw new InvalidRequestParameterException("검색어(keyword)가 필요합니다.");

        if (period == null || period.trim().isEmpty())
            throw new InvalidRequestParameterException("기간(period) 값이 필요합니다.");

        if (!VALID_PERIODS.contains(period))
            throw new InvalidRequestParameterException("기간(period)은 {daily, weekly, monthly} 중 하나여야 합니다.");

        byte[] trend_image = searchService.getKeywordTrendImage(keyword, period);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(trend_image);
    }

    @GetMapping("/analytics_num")
    public ResponseEntity<NumericalTrendResponse> getNumericalKeywordTrendData(@RequestParam String keyword, @RequestParam String period) {

        if (keyword == null || keyword.trim().isEmpty())
            throw new InvalidRequestParameterException("검색어(keyword)가 필요합니다.");

        if (period == null || period.trim().isEmpty())
            throw new InvalidRequestParameterException("기간(period) 값이 필요합니다.");

        if (!VALID_PERIODS.contains(period))
            throw new InvalidRequestParameterException("기간(period)은 {daily, weekly, monthly} 중 하나여야 합니다.");

        NumericalTrendResponse trend_data = searchService.getNumericalAnalyticData(keyword, period);
        return ResponseEntity.ok().body(trend_data);
    }

    @GetMapping("/timeline")
    public ResponseEntity<TimelineResponse> generateKeywordTimeline(@RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty())
            throw new InvalidRequestParameterException("검색어(keyword)가 필요합니다.");

        TimelineResponse timeline_data = searchService.getTimelineOfKeyword(keyword);
        return ResponseEntity.ok().body(timeline_data);
    }
}
