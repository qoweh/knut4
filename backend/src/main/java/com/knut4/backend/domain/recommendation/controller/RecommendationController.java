package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.recommendation.RecommendationService;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recommendation", description = "Menu recommendation APIs using local LLM and Naver map")
@RequestMapping("/api/private/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Get menu recommendations", description = "Generate 3~4 menu suggestions with reasons and nearby places based on weather, moods, budget, and location.")
    public ResponseEntity<RecommendationResponse> recommend(@Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.recommend(request));
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry last recommendation", description = "Regenerate recommendations reusing the last (or specified) history entry conditions.")
    public ResponseEntity<RecommendationResponse> retry(@RequestParam(required = false) Long historyId) {
        return ResponseEntity.ok(recommendationService.retry(historyId));
    }
}
