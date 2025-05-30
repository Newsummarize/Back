// 사용자 정보를 저장하는 JPA 엔티티 클래스
package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// 해당 클래스가 JPA 엔티티이며 "user" 테이블에 매핑됨
@Entity
@Table(name = "user")

// Lombok 어노테이션으로 Getter, Setter, 기본 생성자, AllArgsConstructor, Builder 자동 생성
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // 사용자 고유 ID (기본 키, 자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")  // DB 컬럼명: user_id
    private Long id;

    // 사용자 이름 (not null)
    @Column(name = "user_name", nullable = false)
    private String userName;

    // 사용자 나이 (nullable)
    @Column(nullable = true)
    private int age
            ;

    // 생년월일 (LocalDate 타입 사용, nullable)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // 성별 (enum 타입: M, F, Other → 문자열로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    // 사용자 이메일 (중복 불가, 로그인 ID로 사용 가능)
    @Column(nullable = false, unique = true)
    private String email;

    // 비밀번호 (BCrypt 등의 해시값 저장)
    @Column(nullable = false)
    private String password;

    // 사용자와 관심사 간의 다대다 관계 설정
    // 중간 테이블: user_interest (user_id, interest_id)
    @ManyToMany
    @JoinTable(
            name = "user_interest", // 연결 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 엔티티의 컬럼
            inverseJoinColumns = @JoinColumn(name = "interest_id") // 상대 엔티티의 컬럼
    )
    private Set<Interest> interests = new HashSet<>();
}
