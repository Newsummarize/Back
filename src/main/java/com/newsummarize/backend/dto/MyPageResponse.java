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
    private List<String> interests;
}
