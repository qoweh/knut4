package com.knut4.backend.domain.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
// Default stub when app.llm.mode not specified OR explicitly set to 'stub'
@ConditionalOnProperty(name = "app.llm.mode", havingValue = "stub", matchIfMissing = true)
public class StubLlmClient implements LlmClient {
    private static final List<String> CANDIDATES = List.of("김치찌개", "된장찌개", "비빔밥", "불고기", "파스타", "초밥", "라멘");

    @Override
    public List<LlmMenuSuggestion> suggestMenus(List<String> moods,
                                                String weather,
                                                Integer budget,
                                                Double latitude,
                                                Double longitude,
                                                List<String> nearbyPlaceNames,
                                                int max) {
        List<LlmMenuSuggestion> out = new ArrayList<>();
        String moodSeed = (moods != null && !moods.isEmpty()) ? moods.get(0) : "";
        for (String c : CANDIDATES) {
            if (out.size() >= max) break;
            if (!moodSeed.isBlank() && !c.contains(moodSeed.substring(0,1))) continue;
            String reason = String.format("%s 은/는 %s 날씨와 %s 분위기에 잘 맞고 예산 %s원 범위에서 선택 쉬움", c, weather, moodSeed.isBlank()?"일반":moodSeed, budget==null?"?":budget);
            out.add(new LlmMenuSuggestion(c, reason));
        }
        if (out.isEmpty()) {
            for (String c : CANDIDATES.subList(0, Math.min(max, CANDIDATES.size()))) {
                out.add(new LlmMenuSuggestion(c, String.format("%s 기본 추천", c)));
            }
        }
        return out.subList(0, Math.min(out.size(), max));
    }
}
