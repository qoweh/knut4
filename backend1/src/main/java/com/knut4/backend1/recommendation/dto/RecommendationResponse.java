package com.knut4.backend1.recommendation.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecommendationResponse {
    
    private List<MenuRecommendation> menuRecommendations;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MenuRecommendation {
        private String menuName;
        private String reason;
        private List<Place> places;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Place {
        private String name;
        private Integer distanceMeters;
        private Integer durationMinutes;
        private Double lat;
        private Double lon;
    }
}