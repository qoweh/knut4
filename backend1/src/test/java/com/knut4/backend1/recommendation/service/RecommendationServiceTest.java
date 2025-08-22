package com.knut4.backend1.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend1.recommendation.dto.RecommendationRequest;
import com.knut4.backend1.recommendation.dto.RecommendationResponse;
import com.knut4.backend1.recommendation.provider.MapProvider;
import com.knut4.backend1.recommendation.repository.RecommendationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    
    @Mock
    private MapProvider mapProvider;
    
    @Mock
    private RecommendationHistoryRepository recommendationHistoryRepository;
    
    private RecommendationService recommendationService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        recommendationService = new RecommendationService(
            mapProvider, recommendationHistoryRepository, objectMapper);
    }
    
    @Test
    void generateRecommendations_Success() {
        // Given
        RecommendationRequest request = new RecommendationRequest(
            "cold",
            Arrays.asList("happy", "hungry"),
            20000,
            37.5665,
            126.9780
        );
        
        List<MapProvider.PlaceSearchResult> mockPlaces = Arrays.asList(
            new MapProvider.PlaceSearchResult(
                "테스트 한식당",
                "한식",
                37.5675,
                126.9790,
                "서울시 중구 테스트로 123"
            ),
            new MapProvider.PlaceSearchResult(
                "맛있는 식당",
                "일반한식",
                37.5680,
                126.9785,
                "서울시 중구 맛있는로 456"
            )
        );
        
        when(mapProvider.searchPlaces(anyString(), anyDouble(), anyDouble(), anyInt()))
            .thenReturn(mockPlaces);
        
        // When
        RecommendationResponse response = recommendationService.generateRecommendations(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMenuRecommendations()).isNotEmpty();
        assertThat(response.getMenuRecommendations().size()).isBetween(1, 2);
        
        // Verify menu recommendation structure
        RecommendationResponse.MenuRecommendation menuRec = response.getMenuRecommendations().get(0);
        assertThat(menuRec.getMenuName()).isNotNull();
        assertThat(menuRec.getReason()).isNotNull();
        assertThat(menuRec.getPlaces()).isNotNull();
        
        // Verify places have distance and time calculated
        if (!menuRec.getPlaces().isEmpty()) {
            RecommendationResponse.Place place = menuRec.getPlaces().get(0);
            assertThat(place.getName()).isNotNull();
            assertThat(place.getDistanceMeters()).isGreaterThanOrEqualTo(0);
            assertThat(place.getDurationMinutes()).isGreaterThanOrEqualTo(0);
            assertThat(place.getLat()).isNotNull();
            assertThat(place.getLon()).isNotNull();
        }
    }
    
    @Test
    void generateRecommendations_WithWeatherReasoning() {
        // Given
        RecommendationRequest request = new RecommendationRequest(
            "hot",
            Arrays.asList("refreshed"),
            15000,
            37.5665,
            126.9780
        );
        
        when(mapProvider.searchPlaces(anyString(), anyDouble(), anyDouble(), anyInt()))
            .thenReturn(Arrays.asList());
        
        // When
        RecommendationResponse response = recommendationService.generateRecommendations(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMenuRecommendations()).isNotEmpty();
        
        // Check that reasoning includes weather consideration
        RecommendationResponse.MenuRecommendation menuRec = response.getMenuRecommendations().get(0);
        assertThat(menuRec.getReason()).contains("더운 날씨");
    }
    
    @Test
    void generateRecommendations_WithBudgetReasoning() {
        // Given - Low budget
        RecommendationRequest lowBudgetRequest = new RecommendationRequest(
            null,
            Arrays.asList("hungry"),
            5000,
            37.5665,
            126.9780
        );
        
        when(mapProvider.searchPlaces(anyString(), anyDouble(), anyDouble(), anyInt()))
            .thenReturn(Arrays.asList());
        
        // When
        RecommendationResponse response = recommendationService.generateRecommendations(lowBudgetRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMenuRecommendations()).isNotEmpty();
        
        // Check that reasoning includes budget consideration
        RecommendationResponse.MenuRecommendation menuRec = response.getMenuRecommendations().get(0);
        assertThat(menuRec.getReason()).contains("합리적인 가격");
    }
    
    @Test
    void generateRecommendations_HighBudget() {
        // Given - High budget
        RecommendationRequest highBudgetRequest = new RecommendationRequest(
            null,
            Arrays.asList("special"),
            80000,
            37.5665,
            126.9780
        );
        
        when(mapProvider.searchPlaces(anyString(), anyDouble(), anyDouble(), anyInt()))
            .thenReturn(Arrays.asList());
        
        // When
        RecommendationResponse response = recommendationService.generateRecommendations(highBudgetRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMenuRecommendations()).isNotEmpty();
        
        // Check that reasoning includes premium consideration
        RecommendationResponse.MenuRecommendation menuRec = response.getMenuRecommendations().get(0);
        assertThat(menuRec.getReason()).contains("프리미엄");
    }
    
    @Test
    void generateRecommendations_EmptyPlaces() {
        // Given
        RecommendationRequest request = new RecommendationRequest(
            "rainy",
            Arrays.asList("cozy"),
            25000,
            37.5665,
            126.9780
        );
        
        when(mapProvider.searchPlaces(anyString(), anyDouble(), anyDouble(), anyInt()))
            .thenReturn(Arrays.asList()); // Empty places
        
        // When
        RecommendationResponse response = recommendationService.generateRecommendations(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMenuRecommendations()).isNotEmpty();
        
        // Verify that service handles empty places gracefully
        RecommendationResponse.MenuRecommendation menuRec = response.getMenuRecommendations().get(0);
        assertThat(menuRec.getPlaces()).isEmpty();
    }
}