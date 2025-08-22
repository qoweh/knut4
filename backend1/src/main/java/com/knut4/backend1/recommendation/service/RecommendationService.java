package com.knut4.backend1.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend1.recommendation.dto.RecommendationRequest;
import com.knut4.backend1.recommendation.dto.RecommendationResponse;
import com.knut4.backend1.recommendation.entity.RecommendationHistory;
import com.knut4.backend1.recommendation.provider.MapProvider;
import com.knut4.backend1.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend1.recommendation.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Service for generating menu and restaurant recommendations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final MapProvider mapProvider; // Will inject NaverMapProvider by default
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final ObjectMapper objectMapper;
    
    private static final List<String> DUMMY_MENUS = Arrays.asList(
        "비빔밥", "김치찌개", "불고기", "냉면", "파스타", "피자", "햄버거", "라면", 
        "치킨", "초밥", "카레", "샐러드", "스테이크", "갈비", "된장찌개"
    );
    
    private static final Random random = new Random();
    
    /**
     * Generate recommendations based on request
     */
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        log.info("Generating recommendations for request: {}", request);
        
        // Generate dummy menu recommendations based on input conditions
        List<RecommendationResponse.MenuRecommendation> menuRecommendations = 
            generateMenuRecommendations(request);
        
        RecommendationResponse response = RecommendationResponse.builder()
            .menuRecommendations(menuRecommendations)
            .build();
        
        // Save to history
        saveRecommendationHistory(request, response);
        
        return response;
    }
    
    private List<RecommendationResponse.MenuRecommendation> generateMenuRecommendations(
            RecommendationRequest request) {
        
        // Simple dummy logic - select 1-2 random menus
        int numRecommendations = random.nextInt(2) + 1; // 1 or 2 recommendations
        
        return random.ints(numRecommendations, 0, DUMMY_MENUS.size())
            .distinct()
            .mapToObj(i -> {
                String menuName = DUMMY_MENUS.get(i);
                String reason = generateReason(menuName, request);
                List<RecommendationResponse.Place> places = findNearbyPlaces(menuName, request);
                
                return RecommendationResponse.MenuRecommendation.builder()
                    .menuName(menuName)
                    .reason(reason)
                    .places(places)
                    .build();
            })
            .toList();
    }
    
    private String generateReason(String menuName, RecommendationRequest request) {
        StringBuilder reason = new StringBuilder();
        
        // Add weather-based reasoning
        if (request.getWeather() != null) {
            switch (request.getWeather().toLowerCase()) {
                case "cold", "추위" -> reason.append("추운 날씨에는 따뜻한 ").append(menuName).append("가 좋습니다. ");
                case "hot", "더위" -> reason.append("더운 날씨에는 시원한 ").append(menuName).append("가 좋습니다. ");
                case "rainy", "비" -> reason.append("비 오는 날에는 ").append(menuName).append("와 함께 따뜻한 시간을 보내세요. ");
                default -> reason.append(menuName).append("는 어떤 날씨에도 좋은 선택입니다. ");
            }
        }
        
        // Add mood-based reasoning
        if (request.getMoods() != null && !request.getMoods().isEmpty()) {
            reason.append("현재 기분에 ").append(menuName).append("가 잘 어울립니다. ");
        }
        
        // Add budget consideration
        if (request.getBudget() != null && request.getBudget() > 0) {
            if (request.getBudget() < 10000) {
                reason.append("합리적인 가격으로 즐길 수 있습니다.");
            } else if (request.getBudget() > 50000) {
                reason.append("프리미엄한 경험을 제공합니다.");
            } else {
                reason.append("적정 가격으로 맛있게 드실 수 있습니다.");
            }
        }
        
        return reason.toString().trim();
    }
    
    private List<RecommendationResponse.Place> findNearbyPlaces(
            String menuName, RecommendationRequest request) {
        
        // Search for places using map provider
        String searchKeyword = menuName + " 맛집"; // "menu restaurant"
        List<MapProvider.PlaceSearchResult> searchResults = mapProvider.searchPlaces(
            searchKeyword, request.getLat(), request.getLon(), 2000 // 2km radius
        );
        
        // Convert to response format with distance/time calculation
        return searchResults.stream()
            .limit(3) // Max 3 places per menu
            .map(place -> {
                int[] distanceAndTime = LocationUtils.calculateDistanceAndTime(
                    request.getLat(), request.getLon(), place.lat(), place.lon()
                );
                
                return RecommendationResponse.Place.builder()
                    .name(place.name())
                    .distanceMeters(distanceAndTime[0])
                    .durationMinutes(distanceAndTime[1])
                    .lat(place.lat())
                    .lon(place.lon())
                    .build();
            })
            .toList();
    }
    
    private void saveRecommendationHistory(RecommendationRequest request, RecommendationResponse response) {
        try {
            String moodsJson = objectMapper.writeValueAsString(request.getMoods());
            String recommendationsJson = objectMapper.writeValueAsString(response);
            
            RecommendationHistory history = RecommendationHistory.builder()
                .userId(null) // Anonymous user for now
                .weather(request.getWeather())
                .moods(moodsJson)
                .budget(request.getBudget())
                .lat(request.getLat())
                .lon(request.getLon())
                .recommendations(recommendationsJson)
                .build();
                
            recommendationHistoryRepository.save(history);
            log.info("Saved recommendation history with ID: {}", history.getId());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to save recommendation history", e);
        }
    }
}