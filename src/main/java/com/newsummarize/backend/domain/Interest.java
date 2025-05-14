// 사용자 관심사를 나타내는 JPA 엔티티 클래스
package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;

// 해당 클래스가 DB 테이블과 매핑되는 JPA 엔티티임을 명시
@Entity

// 빌더 패턴을 사용할 수 있게 해주는 Lombok 어노테이션
@Builder

// 이 엔티티가 매핑될 테이블 이름을 명시적으로 설정
@Table(name = "interest")

// Lombok을 이용해 Getter, Setter, 생성자 자동 생성
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interest {

    // 기본 키(PK) 필드 - 자동 증가 전략 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId;

    // 관심사 카테고리 이름 (예: AI, 영화, 부동산 등)
    // - not null 제약
    // - 중복을 방지하기 위해 unique 제약 포함
    @Column(name = "interest_category", nullable = false, unique = true)
    private String interestCategory;

    @Column(nullable = false)
    private boolean isDefault = false; // 초기 제공 관심사 여부

}
