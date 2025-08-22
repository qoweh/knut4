package com.knut4.backend.domain.recommendation.repository;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
}
