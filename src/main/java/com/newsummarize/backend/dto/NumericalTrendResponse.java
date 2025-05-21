package com.newsummarize.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NumericalTrendResponse {
    private String keyword;
    private String period;
    private List<NumericalTrend> results;
}

@Getter
@AllArgsConstructor
class NumericalTrend {
    private String period;
    private Double ratio;
}