package com.knut4.backend1.recommendation.controller;

import com.knut4.backend1.recommendation.dto.RecommendationRequest;
import com.knut4.backend1.recommendation.dto.RecommendationResponse;
import com.knut4.backend1.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for recommendation API
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * Generate menu and restaurant recommendations
     * 
     * @param request recommendation request with user preferences and location
     * @return recommendation response with menu suggestions and nearby places
     */
    @PostMapping
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        
        log.info("Received recommendation request: {}", request);
        
        try {
            RecommendationResponse response = recommendationService.generateRecommendations(request);
            log.info("Generated {} menu recommendations", response.getMenuRecommendations().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating recommendations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}