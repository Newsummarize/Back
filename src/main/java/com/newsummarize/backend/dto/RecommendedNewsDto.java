// 사용자에게 추천할 뉴스 정보를 응답하기 위한 DTO 클래스
package com.newsummarize.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// Lombok을 통해 Getter/Setter 자동 생성
@Getter
@Setter
public class RecommendedNewsDto {

    // 뉴스 고유 ID → JSON에서는 "news_id"로 변환됨
    @JsonProperty("news_id")
    private Long id;

    // 뉴스 제목
    private String title;

    // 뉴스 요약 or 본문
    private String content;

    // 뉴스 제공 언론사
    private String publisher;

    // 게시 일시 → JSON에서는 "published_at"으로 변환됨
    @JsonProperty("published_at")
    private String publishedAt;

    // 뉴스 원문 링크
    private String url;

    // 카테고리 정보 (예: 정치, 경제 등)
    private String category;

    // 썸네일 이미지 주소 → JSON에서는 "image_url"로 변환됨
    @JsonProperty("image_url")
    private String imageUrl;
}
