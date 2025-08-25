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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
    long start = System.nanoTime();
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
            if (resp == null) {
                org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).warn("LLM empty response, using fallback");
                return fallback(moods, weather, max);
            }
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
            org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).error("LLM suggestMenus error: {}", e.toString());
            return fallback(moods, weather, max);
        }
        finally {
            long ms = (System.nanoTime()-start)/1_000_000;
            org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).info("suggestMenus completed in {} ms", ms);
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

    @Override
    @SuppressWarnings("unchecked")
    public List<StructuredMenuPlace> suggestMenusWithPlaces(List<String> moods, String weather, Integer budget, Double latitude, Double longitude, String placeSamplesJson, int menuMax) {
    long start = System.nanoTime();
    String prompt = "당신은 음식 추천 시스템입니다. 아래 JSON 배열(placeSamples)의 장소 리스트를 참고하여 최대 " + menuMax + "개의 메뉴를 제안하고 각 메뉴에 잘 맞는 장소 1~2개를 선택하세요." +
        "\n조건: 날씨="+weather+", 예산="+(budget==null?"?":budget)+", 기분="+(moods==null?"":String.join(",", moods))+"" +
        "\n장소 배열의 각 객체 필드: name(이름), distanceMeters(사용자와의 거리 m), category(추정 카테고리)." +
        "\n반드시 아래 JSON Schema에 맞는 하나의 JSON 배열을 출력하세요. 그 외 텍스트 금지." +
        "\nSchema: [{\"menu\":string, \"reason\":string, \"places\":[{\"name\":string}]}]" +
        "\n제약: reason 25자 내, menu 중복 금지, places 배열은 최대 2개 name만 포함." +
        "\nplaceSamples=" + placeSamplesJson + "\n출력:";
        try {
            Map<String,Object> payload = Map.of(
                    "model", model.isBlank()?"gpt4all":model,
                    "messages", List.of(Map.of("role","user","content", prompt)),
            "max_tokens", 600,
            "temperature", 0.3
            );
            Map<String,Object> resp = webClient.post().uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(25))
                    .onErrorResume(e -> Mono.empty())
                    .block();
            if (resp == null) {
                org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).warn("LLM structured empty response, fallback");
                return fallbackStructured(moods, weather, menuMax);
            }
            Object choices = resp.get("choices");
            if (!(choices instanceof List<?> list) || list.isEmpty()) return fallbackStructured(moods, weather, menuMax);
            Object first = list.get(0);
            String content;
            if (first instanceof Map<?,?> m) {
                Object message = m.get("message");
                if (message instanceof Map<?,?> mm) content = String.valueOf(mm.get("content")); else content = String.valueOf(m.get("text"));
            } else content = first.toString();
            return parseStructuredJson(content, menuMax);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).error("LLM structured error: {}", e.toString());
            return fallbackStructured(moods, weather, menuMax);
        }
        finally {
            long ms = (System.nanoTime()-start)/1_000_000;
            org.slf4j.LoggerFactory.getLogger(HttpOpenAiLikeLlmClient.class).info("suggestMenusWithPlaces completed in {} ms", ms);
        }
    }

    protected List<StructuredMenuPlace> parseStructuredJson(String content, int menuMax) {
        if (content == null) return fallbackStructured(List.of(), "", menuMax);
        String trimmed = content.trim();
        // If model wrapped JSON in code fences or text, extract first JSON array
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start < 0 || end < start) return parseStructured(trimmed, menuMax); // fallback to line parser
        String json = trimmed.substring(start, end+1);
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String,Object>> arr = mapper.readValue(json, new TypeReference<>() {});
            List<StructuredMenuPlace> out = new ArrayList<>();
            for (Map<String,Object> obj : arr) {
                if (out.size() >= menuMax) break;
                Object menuObj = obj.get("menu");
                if (menuObj == null) continue;
                String menu = String.valueOf(menuObj).trim();
                if (menu.isEmpty()) continue;
                String reason = obj.getOrDefault("reason", "추천").toString();
                List<String> places = new ArrayList<>();
                Object placesObj = obj.get("places");
                if (placesObj instanceof List<?> pl) {
                    for (Object p : pl) {
                        if (p instanceof Map<?,?> pm) {
                            Object name = pm.get("name");
                            if (name != null) {
                                String n = name.toString().trim();
                                if (!n.isEmpty()) places.add(n);
                            }
                        } else if (p instanceof String s) {
                            String n = s.trim();
                            if (!n.isEmpty()) places.add(n);
                        }
                        if (places.size() >= 2) break;
                    }
                }
                out.add(new StructuredMenuPlace(menu, places, reason));
            }
            if (!out.isEmpty()) return out;
        } catch (Exception ignore) {
            // fallback
        }
        return parseStructured(trimmed, menuMax);
    }

    private List<StructuredMenuPlace> parseStructured(String content, int menuMax) {
        List<StructuredMenuPlace> out = new ArrayList<>();
        if (content == null) return out;
        for (String raw : content.split("\n")) {
            String line = raw.trim();
            if (line.isBlank()) continue;
            line = line.replaceAll("^[-*0-9. )]+", "");
            String[] parts = line.split("\\|");
            if (parts.length < 1) continue;
            String menu = parts[0].trim();
            List<String> places = List.of();
            String reason = "";
            if (parts.length >= 2) {
                places = java.util.Arrays.stream(parts[1].split(","))
                        .map(String::trim).filter(s->!s.isEmpty()).limit(2).toList();
            }
            if (parts.length >=3) reason = parts[2].trim(); else if (parts.length==2) reason = parts[1].trim();
            if (menu.isEmpty()) continue;
            out.add(new StructuredMenuPlace(menu, places, reason));
            if (out.size() >= menuMax) break;
        }
        return out;
    }

    private List<StructuredMenuPlace> fallbackStructured(List<String> moods, String weather, int menuMax) {
        List<StructuredMenuPlace> list = new ArrayList<>();
        String seed = (moods!=null && !moods.isEmpty())?moods.get(0):"기본";
        for (int i=0;i<menuMax;i++) list.add(new StructuredMenuPlace(seed+"메뉴"+(i+1), List.of(), weather+" 기본"));
        return list;
    }
}
