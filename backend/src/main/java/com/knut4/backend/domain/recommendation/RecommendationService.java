package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.llm.LlmClient;
import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    
    private final MapProvider mapProvider; // injected strategy (Naver for now)
    private final LlmClient llmClient; // injected LLM client (GPT4All or Stub)

    public RecommendationResponse recommend(RecommendationRequest request) {
        // Generate menu recommendations using LLM
        List<LlmClient.MenuRecommendation> llmRecommendations = llmClient.generateMenuRecommendations(
                request.weather(), 
                request.moods(), 
                request.budget(), 
                null // preferences placeholder for future use
        );
        
        // If LLM fails, fallback to first mood-based recommendation
        if (llmRecommendations.isEmpty()) {
            log.debug("LLM returned no recommendations, using fallback");
            String baseMenu = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
            String reason = String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내 후보를 제공합니다", 
                    baseMenu, request.weather(), request.budget());
            llmRecommendations = List.of(new LlmClient.MenuRecommendation(baseMenu, reason));
        }
        
        // For each menu recommendation, search for places
        List<RecommendationResponse.MenuRecommendation> menuRecommendations = llmRecommendations.stream()
                .map(llmRec -> {
                    String keyword = llmRec.menuName() + " 음식";
                    List<PlaceResult> places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
                    List<RecommendationResponse.Place> mappedPlaces = places.stream()
                            .map(p -> new RecommendationResponse.Place(
                                    p.name(), 
                                    p.latitude(), 
                                    p.longitude(), 
                                    p.address(), 
                                    p.distanceMeters(), 
                                    estimateDurationMinutes(p.distanceMeters())
                            ))
                            .collect(Collectors.toList());
                    
                    return new RecommendationResponse.MenuRecommendation(
                            llmRec.menuName(), 
                            llmRec.reason(), 
                            mappedPlaces
                    );
                })
                .collect(Collectors.toList());
        
        return new RecommendationResponse(menuRecommendations);
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }
}
