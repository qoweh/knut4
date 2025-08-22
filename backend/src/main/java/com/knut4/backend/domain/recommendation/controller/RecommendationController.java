package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.auth.service.UserService;
import com.knut4.backend.domain.recommendation.RecommendationService;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RecommendationResponse> recommend(@Valid @RequestBody RecommendationRequest request, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(recommendationService.recommend(request, user));
    }

    @PostMapping("/retry")
    public ResponseEntity<RecommendationResponse> retry(@RequestParam(required = false) Long historyId, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(recommendationService.reRecommend(historyId, user));
    }
}
