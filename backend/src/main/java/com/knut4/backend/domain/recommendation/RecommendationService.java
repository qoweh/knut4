package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.llm.LlmClient;
import com.knut4.backend.domain.llm.LlmMenuSuggestion;
import com.knut4.backend.domain.llm.StructuredMenuPlace;
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
// Micrometer (fully qualified in code if IDE lint issues)
import org.springframework.beans.factory.annotation.Value;

@Service
public class RecommendationService {

    private final MapProvider mapProvider; // injected strategy (currently Naver only)
    private final RecommendationHistoryRepository historyRepository;
    private final LlmClient llmClient; // may be null when disabled
    private final UserRepository userRepository;
    private final SharedRecommendationRepository sharedRepository;
    private final PreferenceRepository preferenceRepository;
    // MeterRegistry kept optional via reflection to avoid hard dependency if micrometer not on classpath in some environments
    private final Object meterRegistry;
    private final boolean historyDedupEnabled;

    public RecommendationService(MapProvider mapProvider,
                                 RecommendationHistoryRepository historyRepository,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) LlmClient llmClient,
                                 UserRepository userRepository,
                                 SharedRecommendationRepository sharedRepository,
                                 PreferenceRepository preferenceRepository,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) Object meterRegistry,
                                 @Value("${app.history.dedup.enabled:true}") boolean historyDedupEnabled) {
        this.mapProvider = mapProvider;
        this.historyRepository = historyRepository;
        this.llmClient = llmClient; // may be null
        this.userRepository = userRepository;
        this.sharedRepository = sharedRepository;
        this.preferenceRepository = preferenceRepository;
        this.meterRegistry = meterRegistry;
        this.historyDedupEnabled = historyDedupEnabled;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
    long tStart = System.nanoTime();
    Object sample = null;
    if (meterRegistry != null) {
        try {
            Class<?> timerCls = Class.forName("io.micrometer.core.instrument.Timer");
            var startMethod = timerCls.getMethod("start", Class.forName("io.micrometer.core.instrument.MeterRegistry"));
            sample = startMethod.invoke(null, meterRegistry);
        } catch (Exception ignore) {}
    }
        String normalizedWeather = normalizeWeather(request.weather());
        org.slf4j.LoggerFactory.getLogger(RecommendationService.class).info("recommend request lat={}, lon={}, weather={}, moods={}, budget={}", request.latitude(), request.longitude(), normalizedWeather, request.moods(), request.budget());
        // derive menu candidates
    List<LlmMenuSuggestion> suggestions;
    List<StructuredMenuPlace> structured = null;
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
            // Prefetch a broader nearby sample with multiple lightweight queries derived from moods for richer LLM context.
            List<PlaceResult> nearbyPlacesFull;
            try {
                java.util.Set<String> acc = new java.util.LinkedHashSet<>();
                acc.addAll(mapProvider.search("맛집", request.latitude(), request.longitude(), 1500).stream().map(PlaceResult::name).toList());
                acc.addAll(mapProvider.search("음식", request.latitude(), request.longitude(), 1500).stream().map(PlaceResult::name).toList());
                if (request.moods() != null) {
                    for (String mood : request.moods()) {
                        String q = moodToQuery(mood);
                        if (q != null) {
                            acc.addAll(mapProvider.search(q, request.latitude(), request.longitude(), 1500).stream().map(PlaceResult::name).toList());
                        }
                    }
                }
                // After name sampling, fetch minimal details for first N distinct names using generic search again (could optimize via caching); for now search each name individually limited to 1 result.
                List<String> nameList = acc.stream().filter(s -> s != null && !s.isBlank()).limit(20).toList();
                java.util.List<PlaceResult> details = new java.util.ArrayList<>();
                for (String n : nameList) {
                    try {
                        var one = mapProvider.search(n, request.latitude(), request.longitude(), 1500);
                        if (!one.isEmpty()) details.add(one.get(0));
                    } catch (Exception ignore) {}
                }
                nearbyPlacesFull = details;
            } catch (Exception e) {
                nearbyPlacesFull = List.of();
            }
            List<String> nearbyNames = nearbyPlacesFull.stream().map(PlaceResult::name).toList();
            // Build place sample JSON with distance/category
            String placeSamplesJson = toPlaceSampleJson(nearbyPlacesFull);
            // Ask for up to 10 raw menus (simple) first as fallback
            suggestions = llmClient.suggestMenus(moodContext, normalizedWeather, request.budget(), request.latitude(), request.longitude(), nearbyNames, 10);
            try {
                structured = llmClient.suggestMenusWithPlaces(moodContext, normalizedWeather, request.budget(), request.latitude(), request.longitude(), placeSamplesJson, 10);
            } catch (Exception ignore) {}
        } else {
            String base = request.moods() != null && !request.moods().isEmpty() ? request.moods().get(0) : "맛있는";
            suggestions = List.of(new LlmMenuSuggestion(base, base + " 기본 추천"));
        }
        // preferences filter (filter by menu name)
        Preference pref = currentUserPreference();
        List<LlmMenuSuggestion> filtered = suggestions;
        boolean filteredOut = false;
        if (pref != null) {
            var dislikes = java.util.Set.of(pref.dislikeArray());
            var allergies = java.util.Set.of(pref.allergyArray());
            filtered = suggestions.stream().filter(s ->
                    dislikes.stream().noneMatch(d -> !d.isBlank() && s.menu().contains(d)) &&
                            allergies.stream().noneMatch(a -> !a.isBlank() && s.menu().contains(a))
            ).toList();
            if (filtered.isEmpty()) { // all filtered -> fallback to original but mark conflict
                filteredOut = true;
                filtered = suggestions;
            }
        }
        final boolean noteConflict = filteredOut;
        // limit to top4 after filtering
        List<LlmMenuSuggestion> top = filtered.stream().limit(4).toList();
        final List<StructuredMenuPlace> structuredFinal = structured;
        List<RecommendationResponse.MenuRecommendation> recs = top.stream().map(s -> {
            List<String> mappedPlaces = extractStructuredPlaces(structuredFinal, s.menu());
            return buildMenuRecommendation(s.menu(), s.reason(), request, normalizedWeather, noteConflict, mappedPlaces);
        }).collect(Collectors.toList());
        persistHistory(request, normalizedWeather, top.isEmpty()? suggestions.get(0).menu() : top.get(0).menu());
    long elapsedMs = (System.nanoTime()-tStart)/1_000_000;
    if (sample != null && meterRegistry != null) {
        try {
            Class<?> timerCls = Class.forName("io.micrometer.core.instrument.Timer");
            var builderMethod = timerCls.getMethod("builder", String.class);
            Object builder = builderMethod.invoke(null, "recommend.pipeline.duration");
            // call description(String)
            var descMethod = builder.getClass().getMethod("description", String.class);
            builder = descMethod.invoke(builder, "Total recommendation pipeline ms");
            // call register(MeterRegistry)
            var registerMethod = builder.getClass().getMethod("register", Class.forName("io.micrometer.core.instrument.MeterRegistry"));
            Object timer = registerMethod.invoke(builder, meterRegistry);
            var sampleCls = Class.forName("io.micrometer.core.instrument.Timer$Sample");
            var stopMethod = sampleCls.getMethod("stop", timerCls);
            stopMethod.invoke(sample, timer);
        } catch (Exception ignore) {}
    }
        org.slf4j.LoggerFactory.getLogger(RecommendationService.class).info("recommend pipeline completed in {} ms (menusRaw={} filtered={})", elapsedMs, suggestions.size(), top.size());
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
        RecommendationHistory h = sr.getHistory();
        // touch lazy fields to initialize before leaving transactional boundary
        if (h != null) {
            h.getWeather();
            h.getMoods();
            h.getBudget();
            h.getLatitude();
            h.getLongitude();
            h.getCreatedAt();
        }
        return h;
    }

    private RecommendationResponse.MenuRecommendation buildMenuRecommendation(String menu, String llmReason, RecommendationRequest request, String normalizedWeather, boolean noteConflict, List<String> preselectedPlaceNames) {
        String keyword = menu + " 음식";
        List<PlaceResult> places;
        try {
            places = mapProvider.search(keyword, request.latitude(), request.longitude(), 1000);
        } catch (Exception e) {
            // fallback to empty list to satisfy non-functional requirement (resilience)
            places = List.of();
        }
        // If structured LLM selected places exist, prioritize them order-wise
        if (preselectedPlaceNames != null && !preselectedPlaceNames.isEmpty()) {
            var map = places.stream().collect(Collectors.toMap(PlaceResult::name, p->p, (a,b)->a));
            List<PlaceResult> prioritized = new java.util.ArrayList<>();
            for (String n : preselectedPlaceNames) {
                if (map.containsKey(n)) prioritized.add(map.get(n));
            }
            // append remaining
            for (PlaceResult p : places) if (prioritized.stream().noneMatch(x->x.name().equals(p.name()))) prioritized.add(p);
            places = prioritized;
        }
        List<RecommendationResponse.Place> mapped = places.stream()
                .map(p -> new RecommendationResponse.Place(p.name(), p.latitude(), p.longitude(), p.address(), p.distanceMeters(), estimateDurationMinutes(p.distanceMeters())))
                .collect(Collectors.toList());
    return new RecommendationResponse.MenuRecommendation(menu, enrichReason(menu, llmReason, normalizedWeather, request.budget(), noteConflict), mapped);
    }

    private double estimateDurationMinutes(double distanceMeters) {
        return Math.round((distanceMeters / 67.0) * 10.0) / 10.0; // 4km/h
    }

    private String enrichReason(String menu, String llmReason, String weather, Integer budget, boolean conflict) {
        StringBuilder sb = new StringBuilder();
        if (llmReason != null && !llmReason.isBlank()) sb.append(llmReason.trim()); else sb.append(menu).append(" 기본 추천");
        sb.append(" | 날씨:").append(weather).append(" 예산:").append(budget).append("원");
        if (conflict) sb.append(" (선호도와 충돌하여 필터 무시)");
        return sb.toString();
    }

    private void persistHistory(RecommendationRequest request, String normalizedWeather, String chosenMenu) {
        try {
            if (historyRepository == null || userRepository == null) return;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User principal = null;
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User p) {
                principal = p;
            }
            // idempotency: if last record (within 2 seconds) has identical weather,moods,budget,lat,lon skip
            if (principal != null && historyDedupEnabled) {
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
            h.setWeather(normalizedWeather);
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
    boolean fieldsEqual = eq(last.getWeather(), normalizeWeather(req.weather())) &&
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
            if (userRepository == null || preferenceRepository == null) return null;
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

    private String toPlaceSampleJson(List<PlaceResult> places) {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<places.size();i++) {
            PlaceResult p = places.get(i);
            if (i>0) sb.append(',');
            sb.append('{')
                    .append("\"name\":\"").append(escape(p.name())).append("\",")
                    .append("\"distanceMeters\":").append(String.format(java.util.Locale.US, "%.1f", p.distanceMeters())).append(',')
                    .append("\"category\":\"").append(escape(inferCategory(p.name()))).append("\"")
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private String escape(String s) { return s==null?"":s.replace("\"","\\\""); }

    private String inferCategory(String name) {
        if (name == null) return "기타";
        String lower = name.toLowerCase();
        if (lower.contains("카페") || lower.contains("coffee") ) return "카페";
        if (lower.contains("치킨")) return "치킨";
        if (lower.contains("피자")) return "피자";
        if (lower.contains("분식")) return "분식";
        if (lower.contains("고기") || lower.contains("삼겹") || lower.contains("갈비")) return "고기";
        if (lower.contains("디저트") || lower.contains("베이커")) return "디저트";
        if (lower.contains("김밥")) return "김밥";
        if (lower.contains("라멘") || lower.contains("라면")) return "라멘";
        if (lower.contains("초밥") || lower.contains("스시")) return "초밥";
        if (lower.contains("한식")) return "한식";
        if (lower.contains("중식")) return "중식";
        if (lower.contains("일식")) return "일식";
        if (lower.contains("양식")) return "양식";
        return "기타";
    }

    private List<String> extractStructuredPlaces(List<StructuredMenuPlace> structured, String menu) {
        if (structured == null) return List.of();
        for (StructuredMenuPlace smp : structured) {
            if (smp.menu().equalsIgnoreCase(menu)) return smp.places();
        }
        return List.of();
    }

    private String normalizeWeather(String weather) {
        return (weather == null || weather.isBlank()) ? "기본" : weather.trim();
    }

    // Map a mood token to an exploratory search keyword to diversify place name sampling.
    private String moodToQuery(String mood) {
        if (mood == null || mood.isBlank()) return null;
        mood = mood.toLowerCase();
        if (mood.contains("매콤")) return "매운맛";
        if (mood.contains("든든")) return "한식";
        if (mood.contains("가볍")) return "샐러드";
        if (mood.contains("달달")) return "디저트";
        return null; // fallback: rely on generic queries
    }
}
