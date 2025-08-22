package com.knut4.backend1.controller;

import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.RecommendationHistoryResponse;
import com.knut4.backend1.service.RecommendationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class RecommendationHistoryController {
    
    private final RecommendationHistoryService recommendationHistoryService;
    
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationHistoryResponse>> getRecommendationHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromAuthentication(authentication);
        
        if (page >= 0 && size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<RecommendationHistoryResponse> historyPage = 
                recommendationHistoryService.getRecommendationHistoryByUser(user, pageable);
            return ResponseEntity.ok(historyPage.getContent());
        } else {
            List<RecommendationHistoryResponse> history = 
                recommendationHistoryService.getRecommendationHistoryByUser(user);
            return ResponseEntity.ok(history);
        }
    }
    
    // For now, return a mock user - in real implementation this would extract from JWT
    private User getUserFromAuthentication(Authentication authentication) {
        // This is a placeholder - in real implementation, you would extract the user from JWT token
        // For tests, we handle null authentication gracefully
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(authentication != null ? authentication.getName() : "testuser");
        mockUser.setEmail("test@example.com");
        return mockUser;
    }
}