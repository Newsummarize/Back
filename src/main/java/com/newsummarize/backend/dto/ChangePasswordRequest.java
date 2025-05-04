// 비밀번호 변경 요청을 처리할 때 사용하는 DTO (Data Transfer Object)
package com.newsummarize.backend.dto;

// record 타입은 불변 객체이며 생성자, getter, equals 등 자동 생성됨
public record ChangePasswordRequest(

        // 사용자가 현재 사용 중인 비밀번호
        String currentPassword,

        // 새로 설정할 비밀번호
        String newPassword,

        // 새 비밀번호 확인 (newPassword와 동일한지 검증용)
        String confirmPassword

) {}
