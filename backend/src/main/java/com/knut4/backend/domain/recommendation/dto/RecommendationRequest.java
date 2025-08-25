package com.knut4.backend.domain.recommendation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RecommendationRequest(
        // Weather made optional (can be null / blank). Service will normalize to "기본".
        String weather,
        @Size(max = 5) List<String> moods,
        @NotNull Integer budget,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}
