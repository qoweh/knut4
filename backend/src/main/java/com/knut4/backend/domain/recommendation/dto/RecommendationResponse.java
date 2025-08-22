package com.knut4.backend.domain.recommendation.dto;

import java.util.List;

public record RecommendationResponse(List<MenuRecommendation> menuRecommendations) {
    public record MenuRecommendation(String menuName, String reason, List<Place> places) {}
    public record Place(String name, double latitude, double longitude, String address, double distanceMeters, double durationMinutes) {}
}
