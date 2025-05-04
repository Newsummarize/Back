package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 뉴스 정보를 저장하는 JPA 엔티티 클래스
@Entity
@Table(name = "news")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    // 기본 키 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    // 뉴스 제목 (최대 500자, not null)
    @Column(length = 500, nullable = false)
    private String title;

    // 뉴스 본문 (TEXT 타입)
    @Lob
    private String content;

    // 뉴스 제공 언론사 (not null)
    @Column(nullable = false)
    private String publisher;

    // 게시 일시 (not null)
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    // 기사 원문 URL (최대 500자, not null)
    @Column(length = 500, nullable = false)
    private String url;

    // 뉴스 카테고리 (예: 정치, 경제, IT 등, not null)
    @Column(nullable = false)
    private String category;

    // 썸네일 이미지 URL (최대 500자, not null)
    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    // 👉 AI 기반 분석 결과 벡터 (임베딩 결과 등 저장), LONGTEXT 사용
    @Lob
    @Column(name = "content_vector")
    private String contentVector;
}
