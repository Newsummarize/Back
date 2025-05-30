package com.newsummarize.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    private String userName;
    private String email;
    private int age;
    private String gender;

    private List<String> defaultInterests;
    private List<String> customInterests;

    // 생년월일 분리 필드 추가
    private int year;
    private int month;
    private int day;
}
