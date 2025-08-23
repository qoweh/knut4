package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.recommendation.RecommendationService;
import com.knut4.backend.domain.recommendation.dto.SharedRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShareController {
    private final RecommendationService recommendationService;

    @PostMapping("/api/private/recommendations/share")
    public ResponseEntity<SharedRecommendationResponse> share(@RequestParam(required = false) Long historyId) {
        var shared = recommendationService.share(historyId);
        return ResponseEntity.ok(SharedRecommendationResponse.from(shared));
    }

    @GetMapping("/api/public/recommendations/shared/{token}")
    public ResponseEntity<SharedRecommendationResponse> getShared(@PathVariable String token) {
        var history = recommendationService.getShared(token);
        return ResponseEntity.ok(new SharedRecommendationResponse(token, history.getId(), history.getWeather(), history.getMoods(), history.getBudget(), history.getLatitude(), history.getLongitude(), history.getCreatedAt()));
    }
}
