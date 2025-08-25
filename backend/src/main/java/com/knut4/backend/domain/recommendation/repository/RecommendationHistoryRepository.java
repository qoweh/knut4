package com.knut4.backend.domain.recommendation.repository;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
	Page<RecommendationHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	Optional<RecommendationHistory> findFirstByUserOrderByCreatedAtDesc(User user);
	long countByUser(User user);
}
