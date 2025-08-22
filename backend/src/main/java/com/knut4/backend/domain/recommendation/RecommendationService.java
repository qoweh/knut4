package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.llm.LlmClient;
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
public class RecommendationService {

    private final MapProvider mapProvider; // injected strategy (Naver or Kakao)
    private final RecommendationHistoryRepository historyRepository;
    private final LlmClient llmClient; // may be null when disabled

    public RecommendationService(MapProvider mapProvider,
                                  RecommendationHistoryRepository historyRepository,
                                  @org.springframework.beans.factory.annotation.Autowired(required = false) LlmClient llmClient) {
        this.mapProvider = mapProvider;
        this.historyRepository = historyRepository;
        this.llmClient = llmClient; // may be null
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        // derive menu candidates
        List<String> menus;
        if (llmClient != null) {
            menus = llmClient.suggestMenus(request.moods(), request.weather(), 3);
        } else {
            String base = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
            menus = List.of(base);
        }
        List<RecommendationResponse.MenuRecommendation> recs = menus.stream().map(menu -> buildMenuRecommendation(menu, request)).collect(Collectors.toList());
        persistHistory(request, menus.get(0));
        return new RecommendationResponse(recs);
    }

    private RecommendationResponse.MenuRecommendation buildMenuRecommendation(String menu, RecommendationRequest request) {
        String keyword = menu + " 음식";
        List<PlaceResult> places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
        return new RecommendationResponse.MenuRecommendation(menu, reason(menu, request.weather(), request.budget()), mapped);
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
