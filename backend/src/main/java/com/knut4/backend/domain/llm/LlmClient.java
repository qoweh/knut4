package com.knut4.backend.domain.llm;

import java.util.List;

/**
 * Interface for LLM clients to generate menu recommendations
 */
public interface LlmClient {
    
    /**
     * Generate menu recommendations based on user preferences
     * @param weather Current weather condition
     * @param moods User's mood preferences
     * @param budget User's budget
     * @param preferences Additional user preferences (optional)
     * @return List of menu recommendation results
     */
    List<MenuRecommendation> generateMenuRecommendations(
            String weather, 
            List<String> moods, 
            Integer budget, 
            String preferences
    );
    
    /**
     * Check if the LLM client is available/healthy
     * @return true if available, false otherwise
     */
    boolean isAvailable();
    
    record MenuRecommendation(String menuName, String reason) {}
}