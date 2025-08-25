package com.knut4.backend.domain.recommendation.dto;

import com.knut4.backend.domain.recommendation.entity.SharedRecommendation;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;

public record SharedRecommendationResponse(String token, Long historyId, String weather, String moods, Integer budget, Double latitude, Double longitude, java.time.Instant createdAt) {
    public static SharedRecommendationResponse from(SharedRecommendation sr) {
        RecommendationHistory h = sr.getHistory();
        return new SharedRecommendationResponse(sr.getToken(), h.getId(), h.getWeather(), h.getMoods(), h.getBudget(), h.getLatitude(), h.getLongitude(), h.getCreatedAt());
    }
}
