package com.knut4.backend.domain.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.llm.enabled", havingValue = "true")
public class StubLlmClient implements LlmClient {
    private static final List<String> CANDIDATES = List.of("김치찌개", "된장찌개", "비빔밥", "불고기", "파스타", "초밥", "라멘");
    @Override
    public List<String> suggestMenus(List<String> moods, String weather, int max) {
        List<String> out = new ArrayList<>();
        for (String c : CANDIDATES) {
            if (out.size() >= max) break;
            if (moods != null && !moods.isEmpty()) {
                if (!c.contains(moods.get(0).substring(0, 1))) continue;
            }
            out.add(c);
        }
        if (out.isEmpty()) out.addAll(CANDIDATES.subList(0, Math.min(max, CANDIDATES.size())));
        return out.subList(0, Math.min(out.size(), max));
    }
}
