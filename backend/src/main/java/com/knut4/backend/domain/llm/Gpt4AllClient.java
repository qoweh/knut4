package com.knut4.backend.domain.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * GPT4All client that communicates with GPT4All sidecar process via HTTP
 */
@Component
@ConditionalOnProperty(name = "app.llm.enabled", havingValue = "true")
@RequiredArgsConstructor
public class Gpt4AllClient implements LlmClient {
    
    private static final Logger log = LoggerFactory.getLogger(Gpt4AllClient.class);
    
    @Value("${app.llm.gpt4all.url:http://gpt4all:4891}")
    private String gpt4allUrl;
    
    @Value("${app.llm.timeout-seconds:3}")
    private int timeoutSeconds;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<MenuRecommendation> generateMenuRecommendations(String weather, List<String> moods, Integer budget, String preferences) {
        try {
            String prompt = buildPrompt(weather, moods, budget, preferences);
            log.debug("Sending prompt to GPT4All: {}", prompt);
            
            Map<String, Object> request = Map.of(
                "prompt", prompt,
                "max_tokens", 200,
                "temperature", 0.7
            );
            
            Mono<String> responseMono = webClient.post()
                    .uri(gpt4allUrl + "/v1/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds));
            
            String response = responseMono.block();
            return parseMenuRecommendations(response);
            
        } catch (Exception e) {
            log.warn("GPT4All request failed: {}", e.getMessage());
            return List.of(); // Return empty list to trigger fallback
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Mono<String> healthCheck = webClient.get()
                    .uri(gpt4allUrl + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(1));
            
            String result = healthCheck.block();
            return result != null;
            
        } catch (Exception e) {
            log.debug("GPT4All health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Build prompt for menu recommendation based on user inputs
     */
    String buildPrompt(String weather, List<String> moods, Integer budget, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Korean food recommendation expert. ");
        prompt.append("Based on the following preferences, recommend exactly 3 different Korean menu items with brief reasons.\n\n");
        
        prompt.append("Weather: ").append(weather).append("\n");
        if (moods != null && !moods.isEmpty()) {
            prompt.append("Mood preferences: ").append(String.join(", ", moods)).append("\n");
        }
        prompt.append("Budget: ").append(budget).append(" KRW\n");
        if (preferences != null && !preferences.trim().isEmpty()) {
            prompt.append("Additional preferences: ").append(preferences).append("\n");
        }
        
        prompt.append("\nFormat your response as exactly 3 lines, each with format:\n");
        prompt.append("MENU: [menu name] | REASON: [brief reason]\n");
        prompt.append("Use Korean menu names and keep reasons under 30 words.");
        
        return prompt.toString();
    }
    
    /**
     * Parse GPT4All response to extract menu recommendations
     */
    List<MenuRecommendation> parseMenuRecommendations(String response) {
        try {
            // Parse the JSON response from GPT4All
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            String content = extractContentFromResponse(jsonResponse);
            
            if (content == null || content.trim().isEmpty()) {
                return List.of();
            }
            
            List<MenuRecommendation> recommendations = new java.util.ArrayList<>();
            String[] lines = content.split("\n");
            
            for (String line : lines) {
                if (line.contains("MENU:") && line.contains("REASON:")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        String menuName = parts[0].replace("MENU:", "").trim();
                        String reason = parts[1].replace("REASON:", "").trim();
                        if (!menuName.isEmpty() && !reason.isEmpty()) {
                            recommendations.add(new MenuRecommendation(menuName, reason));
                        }
                    }
                }
            }
            
            // Return up to 3 recommendations
            return recommendations.stream().limit(3).toList();
            
        } catch (Exception e) {
            log.warn("Failed to parse GPT4All response: {}", e.getMessage());
            return List.of();
        }
    }
    
    @SuppressWarnings("unchecked")
    private String extractContentFromResponse(Map<String, Object> jsonResponse) {
        // Handle different GPT4All response formats
        if (jsonResponse.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonResponse.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                if (firstChoice.containsKey("text")) {
                    return (String) firstChoice.get("text");
                }
                if (firstChoice.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
        }
        
        // Fallback: try direct content field
        if (jsonResponse.containsKey("content")) {
            return (String) jsonResponse.get("content");
        }
        
        return null;
    }
}