// 사용자 회원가입 시 클라이언트가 전송하는 데이터를 담는 DTO 클래스
package com.newsummarize.backend.dto;

import com.newsummarize.backend.domain.Category;
import com.newsummarize.backend.domain.Gender;
import lombok.*;

import java.util.Set;

// Lombok을 사용해 기본 생성자, 전체 생성자, Getter/Setter 자동 생성
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    // 사용자 이름
    private String userName;

    // 생년 정보를 각각 연, 월, 일로 나눠 받음 (birthDate로 조합하여 저장할 수 있음)
    private int year;
    private int month;
    private int day;

    // 사용자 성별 (enum: M, F, Other)
    private Gender gender;

    // 이메일 주소 (계정 ID로 사용됨)
    private String email;

    // 비밀번호 (원문 텍스트로 받음 → 서버에서 암호화 후 저장)
    private String password;

    // 비밀번호 확인 (비교 검증용)
    private String confirmPassword;

    // 사용자가 선택한 관심사 리스트 (예: "AI", "음악", "부동산" 등)
    private Set<String> interests;
}
