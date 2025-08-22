package com.knut4.backend.domain.recommendation;

import com.knut4.backend.common.exception.NotFoundException;
import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MapProvider mapProvider; // injected strategy (Naver for now)
    private final RecommendationHistoryRepository historyRepository;

    @Transactional
    public RecommendationResponse recommend(RecommendationRequest request, User user) {
        RecommendationResponse response = generateRecommendation(request);
        
        // Save recommendation history
        RecommendationHistory history = new RecommendationHistory();
        history.setUser(user);
        history.setWeather(request.weather());
        history.setMoods(request.moods() != null ? String.join(",", request.moods()) : null);
        history.setBudget(request.budget());
        history.setLatitude(request.latitude());
        history.setLongitude(request.longitude());
        historyRepository.save(history);
        
        return response;
    }

    @Transactional
    public RecommendationResponse reRecommend(Long historyId, User user) {
        RecommendationHistory history;
        
        if (historyId != null) {
            // Find specific history by ID and ensure it belongs to the user
            history = historyRepository.findByIdAndUser(historyId, user)
                    .orElseThrow(() -> new NotFoundException("추천 기록을 찾을 수 없습니다."));
        } else {
            // Find the most recent history for the user
            history = historyRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new NotFoundException("이전 추천 기록이 없습니다."));
        }
        
        // Convert history back to RecommendationRequest
        List<String> moods = history.getMoods() != null ? 
                Arrays.asList(history.getMoods().split(",")) : List.of();
        
        RecommendationRequest request = new RecommendationRequest(
                history.getWeather(),
                moods,
                history.getBudget(),
                history.getLatitude(),
                history.getLongitude()
        );
        
        return generateRecommendation(request);
    }

    private RecommendationResponse generateRecommendation(RecommendationRequest request) {
        String baseMenu = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
        String keyword = baseMenu + " 음식";
        List<PlaceResult> places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
        RecommendationResponse.MenuRecommendation mr = new RecommendationResponse.MenuRecommendation(baseMenu, reason(baseMenu, request.weather(), request.budget()), mapped);
        return new RecommendationResponse(List.of(mr));
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }

    private String reason(String menu, String weather, Integer budget) {
        return String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내 후보를 제공합니다", menu, weather, budget);
    }
}
