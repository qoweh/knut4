package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.llm.LlmClient;
import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {
    @Test
    void recommendBuildsResponse() {
        MapProvider mapProvider = mock(MapProvider.class);
        LlmClient llmClient = mock(LlmClient.class);
        
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("PlaceA", 37.0, 127.0, "Addr", 120.0)));
        
        when(llmClient.generateMenuRecommendations(anyString(), any(), anyInt(), any()))
                .thenReturn(List.of(new LlmClient.MenuRecommendation("매콤", "테스트 이유")));

        RecommendationService service = new RecommendationService(mapProvider, llmClient);
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
        RecommendationResponse resp = service.recommend(req);
        
        assertThat(resp.menuRecommendations()).hasSize(1);
        assertThat(resp.menuRecommendations().get(0).places()).hasSize(1);
        assertThat(resp.menuRecommendations().get(0).menuName()).isEqualTo("매콤");
        assertThat(resp.menuRecommendations().get(0).reason()).isEqualTo("테스트 이유");
    }
    
    @Test
    void recommendHandlesLlmFailure() {
        MapProvider mapProvider = mock(MapProvider.class);
        LlmClient llmClient = mock(LlmClient.class);
        
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("PlaceA", 37.0, 127.0, "Addr", 120.0)));
        
        // LLM returns empty list (failure case)
        when(llmClient.generateMenuRecommendations(anyString(), any(), anyInt(), any()))
                .thenReturn(List.of());

        RecommendationService service = new RecommendationService(mapProvider, llmClient);
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
        RecommendationResponse resp = service.recommend(req);
        
        // Should fallback to first mood
        assertThat(resp.menuRecommendations()).hasSize(1);
        assertThat(resp.menuRecommendations().get(0).menuName()).isEqualTo("매콤");
        assertThat(resp.menuRecommendations().get(0).reason()).contains("매콤");
        assertThat(resp.menuRecommendations().get(0).reason()).contains("sunny");
        assertThat(resp.menuRecommendations().get(0).reason()).contains("10000");
    }
}
