package com.knut4.backend1.controller;

import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.PreferenceRequest;
import com.knut4.backend1.dto.PreferenceResponse;
import com.knut4.backend1.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class PreferenceController {
    
    private final PreferenceService preferenceService;
    
    @GetMapping("/preferences")
    public ResponseEntity<PreferenceResponse> getPreferences(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Optional<PreferenceResponse> preference = preferenceService.getPreferenceByUser(user);
        
        return preference
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/preferences")
    public ResponseEntity<PreferenceResponse> savePreferences(
            @Valid @RequestBody PreferenceRequest request,
            Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        PreferenceResponse response = preferenceService.saveOrUpdatePreference(user, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/preferences")
    public ResponseEntity<PreferenceResponse> updatePreferences(
            @Valid @RequestBody PreferenceRequest request,
            Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        PreferenceResponse response = preferenceService.saveOrUpdatePreference(user, request);
        return ResponseEntity.ok(response);
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