package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.recommendation.RecommendationService;
import com.knut4.backend.domain.recommendation.dto.SharedRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/recommendations")
@RequiredArgsConstructor
public class PublicRecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/shared/{token}")
    public ResponseEntity<SharedRecommendationResponse> getSharedRecommendation(@PathVariable String token) {
        return ResponseEntity.ok(recommendationService.getSharedRecommendation(token));
    }
}