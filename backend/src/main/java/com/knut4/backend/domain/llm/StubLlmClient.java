package com.knut4.backend.domain.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub LLM client that returns fallback recommendations when LLM is disabled
 */
@Component
@ConditionalOnProperty(name = "app.llm.enabled", havingValue = "false", matchIfMissing = true)
public class StubLlmClient implements LlmClient {
    
    @Override
    public List<MenuRecommendation> generateMenuRecommendations(String weather, List<String> moods, Integer budget, String preferences) {
        // Return fallback recommendations based on first mood
        String baseMenu = moods != null && !moods.isEmpty() ? moods.get(0) : "맛있는";
        String reason = String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내에서 추천됩니다", 
                baseMenu, weather, budget);
        
        return List.of(new MenuRecommendation(baseMenu, reason));
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Stub is always available
    }
}