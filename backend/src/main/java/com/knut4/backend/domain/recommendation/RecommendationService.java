package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MapProvider mapProvider; // injected strategy (Naver for now)
    private final RecommendationHistoryRepository historyRepository;

    public RecommendationResponse recommend(RecommendationRequest request) {
        String baseMenu = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
        String keyword = baseMenu + " 음식";
        List<PlaceResult> places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
        RecommendationResponse.MenuRecommendation mr = new RecommendationResponse.MenuRecommendation(baseMenu, reason(baseMenu, request.weather(), request.budget()), mapped);
        RecommendationResponse response = new RecommendationResponse(List.of(mr));
        persistHistory(request, baseMenu);
        return response;
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }

    private String reason(String menu, String weather, Integer budget) {
        return String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내 후보를 제공합니다", menu, weather, budget);
    }

    private void persistHistory(RecommendationRequest request, String chosenMenu) {
        try {
            RecommendationHistory h = new RecommendationHistory();
            h.setWeather(request.weather());
            h.setMoods(request.moods() == null ? null : String.join(",", request.moods()));
            h.setBudget(request.budget());
            h.setLatitude(request.latitude());
            h.setLongitude(request.longitude());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // TODO: associate user when integrating user linking (auth principal)
            historyRepository.save(h);
        } catch (Exception ignored) {
            // swallow to avoid disrupting main flow
        }
    }
}
