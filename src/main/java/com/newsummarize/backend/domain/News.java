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

    private LocalDateTime publishedAt;

    private String url;

    private String category;

    private String imageUrl;

    // private int views;
}
