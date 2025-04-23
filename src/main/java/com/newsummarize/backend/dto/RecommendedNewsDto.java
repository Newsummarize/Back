package com.newsummarize.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendedNewsDto {

    @JsonProperty("news_id")
    private Long id;

    private String title;
    private String content;
    private String publisher;

    @JsonProperty("published_at")
    private String publishedAt;

    private String url;
    private String category;

    @JsonProperty("image_url")
    private String imageUrl;
}
