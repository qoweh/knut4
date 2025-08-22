package com.knut4.backend.domain.recommendation;

import com.knut4.backend.common.exception.NotFoundException;
import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    
    @Test
    void reRecommendWithHistoryIdUsesSpecificHistory() {
        MapProvider mapProvider = mock(MapProvider.class);
        RecommendationHistoryRepository historyRepository = mock(RecommendationHistoryRepository.class);
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("PlaceB", 37.1, 127.1, "Addr2", 200.0)));
        
        User user = new User();
        RecommendationHistory history = new RecommendationHistory();
        history.setWeather("rainy");
        history.setMoods("매콤,달콤");
        history.setBudget(15000);
        history.setLatitude(37.2);
        history.setLongitude(126.8);
        
        when(historyRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(history));
        
        RecommendationService service = new RecommendationService(mapProvider, historyRepository);
        RecommendationResponse resp = service.reRecommend(1L, user);
        
        assertThat(resp.menuRecommendations()).hasSize(1);
        verify(historyRepository).findByIdAndUser(1L, user);
        verify(mapProvider).search(anyString(), eq(37.2), eq(126.8), anyInt());
    }
    
    @Test
    void reRecommendWithoutHistoryIdUsesLatestHistory() {
        MapProvider mapProvider = mock(MapProvider.class);
        RecommendationHistoryRepository historyRepository = mock(RecommendationHistoryRepository.class);
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("PlaceC", 37.2, 127.2, "Addr3", 300.0)));
        
        User user = new User();
        RecommendationHistory history = new RecommendationHistory();
        history.setWeather("cloudy");
        history.setMoods("맵지않은");
        history.setBudget(8000);
        history.setLatitude(37.3);
        history.setLongitude(126.7);
        
        when(historyRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(history));
        
        RecommendationService service = new RecommendationService(mapProvider, historyRepository);
        RecommendationResponse resp = service.reRecommend(null, user);
        
        assertThat(resp.menuRecommendations()).hasSize(1);
        verify(historyRepository).findFirstByUserOrderByCreatedAtDesc(user);
        verify(mapProvider).search(anyString(), eq(37.3), eq(126.7), anyInt());
    }
    
    @Test
    void reRecommendWithInvalidHistoryIdThrowsNotFoundException() {
        MapProvider mapProvider = mock(MapProvider.class);
        RecommendationHistoryRepository historyRepository = mock(RecommendationHistoryRepository.class);
        User user = new User();
        
        when(historyRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());
        
        RecommendationService service = new RecommendationService(mapProvider, historyRepository);
        
        assertThatThrownBy(() -> service.reRecommend(999L, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("추천 기록을 찾을 수 없습니다.");
    }
    
    @Test
    void reRecommendWithoutAnyHistoryThrowsNotFoundException() {
        MapProvider mapProvider = mock(MapProvider.class);
        RecommendationHistoryRepository historyRepository = mock(RecommendationHistoryRepository.class);
        User user = new User();
        
        when(historyRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        
        RecommendationService service = new RecommendationService(mapProvider, historyRepository);
        
        assertThatThrownBy(() -> service.reRecommend(null, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("이전 추천 기록이 없습니다.");
    }
}
