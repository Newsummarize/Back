// 로그인 성공 후 클라이언트에게 JWT 토큰을 응답으로 보내기 위한 DTO
package com.newsummarize.backend.dto;

import lombok.*;

// Lombok을 사용해 Getter, Setter, 생성자 자동 생성
@Getter @Setter
@AllArgsConstructor
public class LoginResponse {

    // 클라이언트에게 전달할 JWT 액세스 토큰
    private String token;
}
