package com.knut4.backend.domain.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** HTTP implementation calling an OpenAI-compatible /v1/chat/completions endpoint (GPT4All server). */
@Component
@ConditionalOnProperty(name = "app.llm.mode", havingValue = "http")
public class HttpOpenAiLikeLlmClient implements LlmClient {

    private final WebClient webClient;
    private final String model;

    public HttpOpenAiLikeLlmClient(
        @Value("${llm.openai.base-url:http://localhost:4891/v1}") String baseUrl,
        @Value("${llm.openai.model:}") String model
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.model = model == null || model.isBlank()?"":model;
    }

    record Choice(Map<String,Object> message) {}
    @SuppressWarnings("unchecked")
    @Override
    public List<LlmMenuSuggestion> suggestMenus(List<String> moods, String weather, Integer budget, Double latitude, Double longitude, List<String> nearbyPlaceNames, int max) {
        try {
            String prompt = buildPrompt(moods, weather, budget, latitude, longitude, nearbyPlaceNames, max);
            Map<String,Object> payload = Map.of(
                    "model", model.isBlank()?"gpt4all":model,
                    "messages", List.of(Map.of("role","user","content", prompt)),
                    "max_tokens", 256,
                    "temperature", 0.3
            );
            Map<String,Object> resp = webClient.post().uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(e -> Mono.empty())
                    .block();
            if (resp == null) return fallback(moods, weather, max);
            Object choices = resp.get("choices");
            if (!(choices instanceof List<?> list) || list.isEmpty()) return fallback(moods, weather, max);
            // Expect assistant content with JSON lines or bullet list; we parse naive lines
            Object first = list.get(0);
            String content;
            if (first instanceof Map<?,?> m) {
                Object message = m.get("message");
                if (message instanceof Map<?,?> mm) {
                    content = String.valueOf(mm.get("content"));
                } else content = String.valueOf(m.get("text"));
            } else content = first.toString();
            return parseContent(content, max);
        } catch (Exception e) {
            return fallback(moods, weather, max);
        }
    }

    private String buildPrompt(List<String> moods, String weather, Integer budget, Double lat, Double lon, List<String> nearby, int max) {
        return "You are a Korean food menu recommender. Return up to " + max + " distinct menu items with a short Korean reason.\n" +
                "Output format: one item per line: 메뉴명 - 이유. No numbering.\n" +
                "Moods: " + (moods==null?"":String.join(",", moods)) + "\n" +
                "Weather: " + weather + "\n" +
                "Budget: " + (budget==null?"?":budget) + "\n" +
                "Location: " + lat + "," + lon + "\n" +
                "Nearby place cues: " + (nearby==null?"":String.join(",", nearby));
    }

    private List<LlmMenuSuggestion> parseContent(String content, int max) {
        List<LlmMenuSuggestion> out = new ArrayList<>();
        if (content == null) return out;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            trimmed = trimmed.replaceAll("^[-*0-9. )]+", "");
            String menu;
            String reason;
            int idx = trimmed.indexOf(" - ");
            if (idx > 0) {
                menu = trimmed.substring(0, idx).trim();
                reason = trimmed.substring(idx + 3).trim();
            } else {
                // fallback - split at first space
                int sp = trimmed.indexOf(' ');
                if (sp > 0) { menu = trimmed.substring(0, sp); reason = trimmed.substring(sp+1); }
                else { menu = trimmed; reason = "추천"; }
            }
            if (menu.length() > 40) menu = menu.substring(0, 40);
            out.add(new LlmMenuSuggestion(menu, reason));
            if (out.size() >= max) break;
        }
        return out;
    }

    private List<LlmMenuSuggestion> fallback(List<String> moods, String weather, int max) {
        List<LlmMenuSuggestion> list = new ArrayList<>();
        String seed = (moods!=null && !moods.isEmpty())?moods.get(0):"기본";
        for (int i=0;i<max;i++) list.add(new LlmMenuSuggestion(seed+"메뉴"+(i+1), weather+" 날씨 기본"));
        return list;
    }
}
