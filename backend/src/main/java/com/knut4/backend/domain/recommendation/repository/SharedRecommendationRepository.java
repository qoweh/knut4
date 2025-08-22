package com.knut4.backend.domain.recommendation.repository;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.entity.SharedRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedRecommendationRepository extends JpaRepository<SharedRecommendation, Long> {
    Optional<SharedRecommendation> findByToken(String token);
    Optional<SharedRecommendation> findByHistory(RecommendationHistory history);
}