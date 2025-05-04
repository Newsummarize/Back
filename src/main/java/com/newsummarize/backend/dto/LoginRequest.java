// 로그인 요청 시 클라이언트로부터 이메일과 비밀번호를 입력받는 DTO
package com.newsummarize.backend.dto;

import lombok.*;

// Lombok 어노테이션으로 생성자, Getter, Setter 자동 생성
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // 사용자 이메일 (로그인 ID로 사용됨)
    private String email;

    // 사용자 비밀번호 (일반 텍스트로 입력받고, 서버에서 검증 및 암호화 처리)
    private String password;
}
