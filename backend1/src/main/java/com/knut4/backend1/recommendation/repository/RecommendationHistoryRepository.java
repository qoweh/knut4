package com.knut4.backend1.recommendation.repository;

import com.knut4.backend1.recommendation.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for RecommendationHistory entity
 */
@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    
    /**
     * Find recommendations by user ID
     * @param userId user ID (can be null for anonymous users)
     * @return list of recommendation history
     */
    List<RecommendationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find recent recommendations within time range
     * @param after start time
     * @return list of recent recommendations
     */
    List<RecommendationHistory> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
}