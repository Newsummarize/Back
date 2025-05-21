// 뉴스 데이터를 처리하는 JPA 리포지토리 인터페이스
package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// News 엔티티를 대상으로 기본 CRUD 및 사용자 정의 쿼리를 처리
public interface NewsRepository extends JpaRepository<News, Long> {

    // 1. 뉴스 테이블에서 랜덤으로 8개 기사 조회 (메인 뉴스용)
    @Query(value = "SELECT * FROM news ORDER BY RAND() LIMIT 8", nativeQuery = true)
    List<News> findRandom8News();

    // 2. 특정 카테고리 뉴스 중 랜덤으로 2개만 조회 (관심사 기반 추천용)
    @Query(value = "SELECT * FROM news WHERE category = :category ORDER BY RAND() LIMIT 2", nativeQuery = true)
    List<News> findByCategoryLimit2(String category);

    // 3. 이미 저장된 뉴스의 URL만 모두 추출 (크롤링 중복 방지용)
    @Query("SELECT n.url FROM News n")
    Set<String> findAllUrls();

    Optional<News> findByUrl(String url);
}
