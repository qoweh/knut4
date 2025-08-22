package com.knut4.backend1.service;

import com.knut4.backend1.domain.RecommendationHistory;
import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.RecommendationHistoryResponse;
import com.knut4.backend1.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationHistoryService {
    
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    
    public RecommendationHistory saveRecommendationHistory(User user, List<String> recommendedItems, String context) {
        RecommendationHistory history = new RecommendationHistory(user, recommendedItems, context);
        return recommendationHistoryRepository.save(history);
    }
    
    @Transactional(readOnly = true)
    public List<RecommendationHistoryResponse> getRecommendationHistoryByUser(User user) {
        List<RecommendationHistory> histories = recommendationHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        return histories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<RecommendationHistoryResponse> getRecommendationHistoryByUser(User user, Pageable pageable) {
        Page<RecommendationHistory> histories = recommendationHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return histories.map(this::mapToResponse);
    }
    
    private RecommendationHistoryResponse mapToResponse(RecommendationHistory history) {
        return new RecommendationHistoryResponse(
                history.getId(),
                history.getRecommendedItems(),
                history.getContext(),
                history.getCreatedAt()
        );
    }
}