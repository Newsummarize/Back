// 마이페이지 조회 시 사용자 정보를 클라이언트에 응답하기 위한 DTO
package com.newsummarize.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Getter 자동 생성
@Getter

// 빌더 패턴 사용 가능 (MyPageResponse.builder()...)
@Builder
public class MyPageResponse {

    // 사용자 이름
    private String userName;

    // 사용자 이메일 주소
    private String email;

    // 사용자 나이
    private int age;

    // 사용자 성별 ("M", "F", "Other" 중 하나의 문자열)
    private String gender;

    // 초기 제공된 관심사
    private List<String> defaultInterests;

    // 사용자가 추가한 관심사
    private List<String> customInterests;
}
