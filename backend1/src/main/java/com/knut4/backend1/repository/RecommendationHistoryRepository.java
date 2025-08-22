package com.knut4.backend1.repository;

import com.knut4.backend1.domain.RecommendationHistory;
import com.knut4.backend1.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    
    List<RecommendationHistory> findByUserOrderByCreatedAtDesc(User user);
    
    Page<RecommendationHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<RecommendationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}