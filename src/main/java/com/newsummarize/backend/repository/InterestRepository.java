// Interest 엔티티에 대한 데이터베이스 접근을 처리하는 JPA 리포지토리 인터페이스
package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository를 확장하여 Interest 엔티티의 CRUD 기능을 자동 제공
public interface InterestRepository extends JpaRepository<Interest, Long> {

    // 관심사 카테고리명으로 관심사 객체를 조회 (Optional로 감싸 null 처리 안전하게)
    Optional<Interest> findByInterestCategory(String interestCategory);
}
