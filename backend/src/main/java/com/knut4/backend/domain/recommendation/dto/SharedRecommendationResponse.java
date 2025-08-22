package com.knut4.backend.domain.recommendation.dto;

import java.time.Instant;

public record SharedRecommendationResponse(
        String weather,
        String moods,
        Integer budget,
        Double latitude,
        Double longitude,
        Instant createdAt,
        String message
) {
    public static SharedRecommendationResponse from(
            String weather,
            String moods,
            Integer budget,
            Double latitude,
            Double longitude,
            Instant createdAt
    ) {
        return new SharedRecommendationResponse(
                weather,
                moods,
                budget,
                latitude,
                longitude,
                createdAt,
                "이 추천 결과를 새로 생성하려면 동일한 조건으로 추천을 요청해보세요."
        );
    }
}