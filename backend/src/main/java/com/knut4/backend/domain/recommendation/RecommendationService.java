package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.llm.LlmClient;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.preference.repository.PreferenceRepository;
import com.knut4.backend.domain.preference.entity.Preference;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.entity.SharedRecommendation;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.recommendation.repository.SharedRecommendationRepository;
import com.knut4.backend.domain.user.UserRepository;
import com.knut4.backend.common.exception.ResourceNotFoundException;
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
    private final UserRepository userRepository;
    private final SharedRecommendationRepository sharedRepository;
    private final PreferenceRepository preferenceRepository;

    public RecommendationService(MapProvider mapProvider,
                                 RecommendationHistoryRepository historyRepository,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) LlmClient llmClient,
                                 UserRepository userRepository,
                                 SharedRecommendationRepository sharedRepository,
                                 PreferenceRepository preferenceRepository) {
        this.mapProvider = mapProvider;
        this.historyRepository = historyRepository;
        this.llmClient = llmClient; // may be null
        this.userRepository = userRepository;
        this.sharedRepository = sharedRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        // derive menu candidates
        List<String> menus;
        if (llmClient != null) {
            // Future: augment LLM prompt with extended preferences (likes, diet types, spice levels, notes)
            // Currently LlmClient interface only supports moods & weather; enhancement would require interface change.
            // We can bias moods list by appending first like or diet token to broaden context.
            Preference pref = currentUserPreference();
            List<String> moodContext = request.moods();
            if (pref != null) {
                String firstLike = (pref.getLikes() != null && !pref.getLikes().isBlank()) ? pref.getLikes().split(",")[0].trim() : null;
                if (firstLike != null && !firstLike.isBlank()) {
                    // append lightweight signal if not already present
                    if (moodContext == null || moodContext.isEmpty()) {
                        moodContext = List.of(firstLike);
                    } else if (!moodContext.contains(firstLike)) {
                        moodContext = new java.util.ArrayList<>(moodContext);
                        ((java.util.ArrayList<String>) moodContext).add(firstLike);
                    }
                }
            }
            menus = llmClient.suggestMenus(moodContext, request.weather(), 4);
        } else {
            String base = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
            menus = List.of(base);
        }
        // preferences filter
        Preference pref = currentUserPreference();
        List<String> filtered = menus;
        boolean filteredOut = false;
        if (pref != null) {
            var dislikes = java.util.Set.of(pref.dislikeArray());
            var allergies = java.util.Set.of(pref.allergyArray());
            filtered = menus.stream().filter(m ->
                    dislikes.stream().noneMatch(d -> !d.isBlank() && m.contains(d)) &&
                            allergies.stream().noneMatch(a -> !a.isBlank() && m.contains(a))
            ).toList();
            if (filtered.isEmpty()) { // all filtered -> fallback to original but mark conflict
                filteredOut = true;
                filtered = menus;
            }
        }
        final boolean noteConflict = filteredOut;
        List<RecommendationResponse.MenuRecommendation> recs = filtered.stream().map(menu -> buildMenuRecommendation(menu, request, noteConflict)).collect(Collectors.toList());
    persistHistory(request, menus.get(0));
        return new RecommendationResponse(recs);
    }

    public RecommendationResponse retry(Long historyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User principal)) {
            throw new IllegalArgumentException("Unauthenticated");
        }
        String username = principal.getUsername();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        RecommendationHistory baseHistory;
        if (historyId != null) {
            baseHistory = historyRepository.findById(historyId)
                    .filter(h -> h.getUser() != null && h.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("History not found"));
        } else {
            baseHistory = historyRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new ResourceNotFoundException("No history"));
        }
        RecommendationRequest request = new RecommendationRequest(
                baseHistory.getWeather(),
                baseHistory.getMoods() == null ? List.of() : List.of(baseHistory.getMoods().split(",")),
                baseHistory.getBudget(),
                baseHistory.getLatitude(),
                baseHistory.getLongitude()
        );
        return recommend(request);
    }

    public SharedRecommendation share(Long historyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User principal)) {
            throw new IllegalArgumentException("Unauthenticated");
        }
        var user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RecommendationHistory history;
        if (historyId != null) {
            history = historyRepository.findById(historyId)
                    .filter(h -> h.getUser() != null && h.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("History not found"));
        } else {
            history = historyRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new ResourceNotFoundException("No history"));
        }
        return sharedRepository.findByHistory_Id(history.getId())
                .orElseGet(() -> {
                    SharedRecommendation sr = new SharedRecommendation();
                    sr.setHistory(history);
                    sr.setUser(user);
                    return sharedRepository.save(sr);
                });
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RecommendationHistory getShared(String token) {
        SharedRecommendation sr = sharedRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share token not found"));
        return sr.getHistory();
    }

    private RecommendationResponse.MenuRecommendation buildMenuRecommendation(String menu, RecommendationRequest request, boolean noteConflict) {
        String keyword = menu + " 음식";
        List<PlaceResult> places;
        try {
            places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        } catch (Exception e) {
            // fallback to empty list to satisfy non-functional requirement (resilience)
            places = List.of();
        }
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
        return new RecommendationResponse.MenuRecommendation(menu, reason(menu, request.weather(), request.budget(), noteConflict), mapped);
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }

    private String reason(String menu, String weather, Integer budget, boolean conflict) {
        String base = String.format("'%s' 메뉴는 현재 날씨(%s)에 잘 어울리고 예산 %d원 범위 내 후보를 제공합니다", menu, weather, budget);
        if (conflict) base += " (선호도와 충돌하여 기본 추천 유지)";
        return base;
    }

    private void persistHistory(RecommendationRequest request, String chosenMenu) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User principal = null;
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User p) {
                principal = p;
            }
            // idempotency: if last record (within 2 seconds) has identical weather,moods,budget,lat,lon skip
            if (principal != null) {
                var userOpt = userRepository.findByUsername(principal.getUsername());
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
            // normalize moods string
            String normalizedMoods = request.moods() == null ? null : request.moods().stream()
                .map(String::trim).filter(s -> !s.isEmpty()).reduce((a,b) -> a+","+b).orElse("");
            if (normalizedMoods != null && normalizedMoods.isBlank()) normalizedMoods = null;
            // fetch recent history list (avoid adding new query method -> simple stream on existing finder)
            var lastOpt = historyRepository.findFirstByUserOrderByCreatedAtDesc(user);
            if (lastOpt.isPresent()) {
            var last = lastOpt.get();
            if (isSameRequest(last, request, normalizedMoods)) return;
            }
                }
            }
            RecommendationHistory h = new RecommendationHistory();
            h.setWeather(request.weather());
            h.setMoods(request.moods() == null ? null : String.join(",", request.moods()));
            h.setBudget(request.budget());
            h.setLatitude(request.latitude());
            h.setLongitude(request.longitude());
            if (principal != null) {
                userRepository.findByUsername(principal.getUsername()).ifPresent(h::setUser);
            }
            historyRepository.save(h);
        } catch (Exception ignored) {
            // swallow to avoid disrupting main flow
        }
    }

    private boolean eq(Object a, Object b) { return (a == null && b == null) || (a != null && a.equals(b)); }

    private boolean isSameRequest(RecommendationHistory last, RecommendationRequest req, String normalizedMoods) {
    String lastMoodsNorm = last.getMoods() == null ? null : java.util.Arrays.stream(last.getMoods().split(","))
        .map(String::trim).filter(s->!s.isEmpty()).reduce((a,b)->a+","+b).orElse("");
    if (lastMoodsNorm != null && lastMoodsNorm.isBlank()) lastMoodsNorm = null;
    boolean fieldsEqual = eq(last.getWeather(), req.weather()) &&
        eq(lastMoodsNorm, normalizedMoods) &&
        eq(last.getBudget(), req.budget()) &&
        eq(last.getLatitude(), req.latitude()) &&
        eq(last.getLongitude(), req.longitude());
    if (!fieldsEqual) return false;
    long diffMillis = Math.abs(java.time.Duration.between(last.getCreatedAt(), java.time.Instant.now()).toMillis());
    return diffMillis < 2000; // 2 seconds window
    }

    private Preference currentUserPreference() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User principal) {
                var userOpt = userRepository.findByUsername(principal.getUsername());
                if (userOpt.isPresent()) {
                    return preferenceRepository.findByUser(userOpt.get()).orElse(null);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
