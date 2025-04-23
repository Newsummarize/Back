package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByInterestCategory(String interestCategory);
}
