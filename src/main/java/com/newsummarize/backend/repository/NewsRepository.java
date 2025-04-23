package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface NewsRepository extends JpaRepository<News, Long> {

    // 랜덤 뉴스 8개 조회
    @Query(value = "SELECT * FROM news ORDER BY RAND() LIMIT 8", nativeQuery = true)
    List<News> findRandom8News();

    // 지정한 카테고리 기반으로 랜덤 뉴스 2개 조회
    @Query(value = "SELECT * FROM news WHERE category = :category ORDER BY RAND() LIMIT 2", nativeQuery = true)
    List<News> findByCategoryLimit2(String category);

    @Query("SELECT n.url FROM News n")
    Set<String> findAllUrls(); // 기존에 저장된 URL들 조회

}
