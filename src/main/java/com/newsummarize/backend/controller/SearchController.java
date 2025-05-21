package com.newsummarize.backend.controller;

import com.newsummarize.backend.domain.News;
import com.newsummarize.backend.dto.ErrorResponse;
import com.newsummarize.backend.dto.NumericalTrendResponse;
import com.newsummarize.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> getSearchedNews(@RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("[Log] >>> 검색어가 누락된 요청");
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", "검색어(keyword)가 필요합니다.")
            );
        }

        List<News> news = searchService.getSearchedNews(keyword);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getKeywordTrendImage(@RequestParam String keyword, @RequestParam String period) {

        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("[Log] >>> 검색어가 누락된 요청");
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", "검색어(keyword)가 필요합니다.")
            );
        }
        if (period == null || period.trim().isEmpty()) {
            System.out.println("[Log] >>> 기간 값이 누락된 요청");
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", "기간(period) 값이 필요합니다.")
            );
        }

        try {
            byte[] trend_image = searchService.getKeywordTrendImage(keyword, period);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(trend_image);
        } catch (IllegalArgumentException exc) {
            System.out.println("[Error] >>> " + exc.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", exc.getMessage())
            );
        } catch (RuntimeException ex) {
            System.out.println("[Error] >>> 서버 내부에 문제가 발생했습니다: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Internal Server Error", "서버 내부에 문제가 발생했습니다.")
            );
        } catch (Exception e) {
            System.out.println("[Error] >>> 예상치 못한 문제가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Internal Server Error", "예상치 못한 문제가 발생했습니다.")
            );
        }
    }

    @GetMapping("/analytics_num")
    public ResponseEntity<?> getNumericalKeywordTrendData(@RequestParam String keyword, @RequestParam String period) {

        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("[Log] >>> 검색어가 누락된 요청");
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", "검색어(keyword)가 필요합니다.")
            );
        }
        if (period == null || period.trim().isEmpty()) {
            System.out.println("[Log] >>> 기간 값이 누락된 요청");
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", "기간(period) 값이 필요합니다.")
            );
        }

        try {
            NumericalTrendResponse trend_data = searchService.getNumericalAnalyticData(keyword, period);
            return ResponseEntity.ok(trend_data);
        } catch (IllegalArgumentException exc) {
            System.out.println("[Error] >>> " + exc.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, "Bad Request", exc.getMessage())
            );
        } catch (RuntimeException ex) {
            System.out.println("[Error] >>> 서버 내부에 문제가 발생했습니다: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Internal Server Error", "서버 내부에 문제가 발생했습니다.")
            );
        } catch (Exception e) {
            System.out.println("[Error] >>> 예상치 못한 문제가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Internal Server Error", "예상치 못한 문제가 발생했습니다.")
            );
        }
    }
}
