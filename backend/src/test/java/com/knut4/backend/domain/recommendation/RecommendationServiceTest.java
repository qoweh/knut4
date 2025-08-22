package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {
    @Test
    void recommendBuildsResponse() {
        MapProvider mapProvider = mock(MapProvider.class);
        RecommendationHistoryRepository historyRepository = mock(RecommendationHistoryRepository.class);
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("PlaceA", 37.0, 127.0, "Addr", 120.0)));

        RecommendationService service = new RecommendationService(mapProvider, historyRepository);
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
        User user = new User();
        RecommendationResponse resp = service.recommend(req, user);
        assertThat(resp.menuRecommendations()).hasSize(1);
        assertThat(resp.menuRecommendations().get(0).places()).hasSize(1);
        
        // Verify that history was saved
        verify(historyRepository, times(1)).save(any());
    }
}
