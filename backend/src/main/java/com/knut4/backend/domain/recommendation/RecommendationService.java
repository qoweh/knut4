package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.dto.ShareRecommendationResponse;
import com.knut4.backend.domain.recommendation.dto.SharedRecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.entity.SharedRecommendation;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.recommendation.repository.SharedRecommendationRepository;
import com.knut4.backend.domain.user.User;
import com.knut4.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MapProvider mapProvider; // injected strategy (Naver for now)
    private final RecommendationHistoryRepository historyRepository;
    private final SharedRecommendationRepository sharedRecommendationRepository;

    @Transactional
    public RecommendationResponse recommend(RecommendationRequest request, User user) {
        String baseMenu = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
        String keyword = baseMenu + " 음식";
        List<PlaceResult> places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
        RecommendationResponse.MenuRecommendation mr = new RecommendationResponse.MenuRecommendation(baseMenu, reason(baseMenu, request.weather(), request.budget()), mapped);
        
        // Save history
        RecommendationHistory history = new RecommendationHistory();
        history.setUser(user);
        history.setWeather(request.weather());
        history.setMoods(request.moods() != null ? String.join(",", request.moods()) : null);
        history.setBudget(request.budget());
        history.setLatitude(request.latitude());
        history.setLongitude(request.longitude());
        historyRepository.save(history);
        
        return new RecommendationResponse(List.of(mr));
    }

    @Transactional
    public ShareRecommendationResponse shareRecommendation(Long historyId, User user) {
        RecommendationHistory history;
        
        if (historyId != null) {
            // Find specific history and validate ownership
            history = historyRepository.findByIdAndUser(historyId, user)
                    .orElseThrow(() -> new NotFoundException("추천 기록을 찾을 수 없습니다."));
        } else {
            // Use latest history for user
            history = historyRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new NotFoundException("공유할 추천 기록이 없습니다."));
        }
        
        // Check if already shared, return existing token
        return sharedRecommendationRepository.findByHistory(history)
                .map(shared -> new ShareRecommendationResponse(shared.getToken()))
                .orElseGet(() -> {
                    // Create new shared recommendation
                    SharedRecommendation shared = new SharedRecommendation();
                    shared.setUser(user);
                    shared.setHistory(history);
                    sharedRecommendationRepository.save(shared);
                    return new ShareRecommendationResponse(shared.getToken());
                });
    }

    public SharedRecommendationResponse getSharedRecommendation(String token) {
        SharedRecommendation shared = sharedRecommendationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("공유된 추천을 찾을 수 없습니다."));
        
        RecommendationHistory history = shared.getHistory();
        return SharedRecommendationResponse.from(
                history.getWeather(),
                history.getMoods(),
                history.getBudget(),
                history.getLatitude(),
                history.getLongitude(),
                history.getCreatedAt()
        );
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }

    private String reason(String menu, String weather, Integer budget) {
        return String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내 후보를 제공합니다", menu, weather, budget);
    }
}
