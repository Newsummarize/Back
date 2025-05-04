// 관심사 추가 또는 삭제 요청 시 사용되는 DTO 클래스
package com.newsummarize.backend.dto;

import lombok.Getter;

// Lombok을 사용해 Getter 메서드 자동 생성
@Getter
public class InterestRequest {

    // 클라이언트에서 전송한 관심사 문자열 (예: "AI", "음악", "영화" 등)
    private String interest;
}
