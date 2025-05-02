package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    private String title;

    @Column(length = 1000)
    private String content;

    private String publisher;

    private LocalDateTime published_at;

    private String url;

    private String category;

    private String imageUrl;


    // 👉 벡터 값 5개 추가
    @Column(name = "keyword_vector_1")
    private Float keywordVector1;

    @Column(name = "keyword_vector_2")
    private Float keywordVector2;

    @Column(name = "keyword_vector_3")
    private Float keywordVector3;

    @Column(name = "keyword_vector_4")
    private Float keywordVector4;

    @Column(name = "keyword_vector_5")
    private Float keywordVector5;

}
