package com.knut4.backend1.service;

import com.knut4.backend1.domain.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationFilterService {
    
    private final PreferenceService preferenceService;
    
    /**
     * Filters recommendations based on user preferences (allergies and dislikes)
     * Simple string matching approach for current implementation
     */
    public List<String> filterRecommendations(com.knut4.backend1.domain.User user, List<String> originalRecommendations) {
        var preferenceOpt = preferenceService.getPreferenceEntityByUser(user);
        
        if (preferenceOpt.isEmpty()) {
            return originalRecommendations;
        }
        
        Preference preference = preferenceOpt.get();
        List<String> allergies = preference.getAllergies();
        List<String> dislikes = preference.getDislikes();
        
        return originalRecommendations.stream()
                .filter(recommendation -> !containsAnyAllergy(recommendation, allergies))
                .filter(recommendation -> !containsAnyDislike(recommendation, dislikes))
                .collect(Collectors.toList());
    }
    
    private boolean containsAnyAllergy(String recommendation, List<String> allergies) {
        if (allergies == null || allergies.isEmpty()) {
            return false;
        }
        
        String lowerRecommendation = recommendation.toLowerCase();
        return allergies.stream()
                .filter(allergy -> allergy != null && !allergy.trim().isEmpty())
                .anyMatch(allergy -> {
                    String lowerAllergy = allergy.toLowerCase();
                    // Check both directions: recommendation contains allergy, or allergy root matches ingredient
                    return lowerRecommendation.contains(lowerAllergy) || 
                           matchesIngredient(lowerRecommendation, lowerAllergy);
                });
    }
    
    private boolean containsAnyDislike(String recommendation, List<String> dislikes) {
        if (dislikes == null || dislikes.isEmpty()) {
            return false;
        }
        
        String lowerRecommendation = recommendation.toLowerCase();
        return dislikes.stream()
                .filter(dislike -> dislike != null && !dislike.trim().isEmpty())
                .anyMatch(dislike -> {
                    String lowerDislike = dislike.toLowerCase();
                    // Check both directions: recommendation contains dislike, or dislike root matches ingredient
                    return lowerRecommendation.contains(lowerDislike) || 
                           matchesIngredient(lowerRecommendation, lowerDislike);
                });
    }
    
    private boolean matchesIngredient(String recommendation, String allergyOrDislike) {
        // Handle common cases like "peanuts" matching "peanut"
        String allergyRoot = allergyOrDislike.endsWith("s") ? 
            allergyOrDislike.substring(0, allergyOrDislike.length() - 1) : allergyOrDislike;
        
        return recommendation.contains(allergyRoot);
    }
}