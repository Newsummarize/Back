package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 뉴스 정보를 저장하는 JPA 엔티티 클래스입니다.
 * DB 테이블 'news'와 매핑됩니다.
 */
@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    /** 뉴스 제목 - 최대 500자, Not Null */
    @Column(length = 500, nullable = false)
    private String title;

    /** 뉴스 본문 - TEXT 타입 */
    @Lob
    private String content;

    /** 뉴스 제공 언론사 - 최대 255자, Not Null */
    @Column(nullable = false, length = 255)
    private String publisher;

    /** 게시 일시 - DATETIME, Not Null (컬럼명: published_at) */
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    /** 기사 원문 URL - 최대 500자, Not Null */
    @Column(length = 500, nullable = false)
    private String url;

    /** 뉴스 카테고리 (예: 정치, 경제 등) - 최대 255자, Not Null */
    @Column(nullable = false, length = 255)
    private String category;

    /** 썸네일 이미지 URL - 최대 500자, Not Null (컬럼명: image_url) */
    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    /** AI 임베딩 벡터 결과 - LONGTEXT 타입 (컬럼명: content_vector) */
    @Lob
    @Column(name = "content_vector", columnDefinition = "LONGTEXT")
    private String contentVector;
}
