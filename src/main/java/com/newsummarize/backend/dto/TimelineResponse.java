package com.newsummarize.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TimelineResponse {
    private String keyword;
    private int total;
    private List<KeywordEvent> events;
}

@Getter
@AllArgsConstructor
class KeywordEvent {
    private int id;
    private String title;
    private LocalDateTime publishedAt;
    private String content;
}