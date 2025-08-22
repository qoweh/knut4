package com.knut4.backend.domain.recommendation.repository;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    Optional<RecommendationHistory> findByIdAndUser(Long id, User user);
    Optional<RecommendationHistory> findFirstByUserOrderByCreatedAtDesc(User user);
}