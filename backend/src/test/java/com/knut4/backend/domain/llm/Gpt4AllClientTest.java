package com.knut4.backend.domain.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class Gpt4AllClientTest {
    
    private Gpt4AllClient gpt4AllClient;
    
    @BeforeEach
    void setUp() {
        WebClient webClient = mock(WebClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        gpt4AllClient = new Gpt4AllClient(webClient, objectMapper);
    }
    
    @Test
    void buildPromptIncludesAllParameters() {
        String prompt = gpt4AllClient.buildPrompt("sunny", List.of("매콤", "따뜻한"), 15000, "채식주의");
        
        assertThat(prompt).contains("Weather: sunny");
        assertThat(prompt).contains("Mood preferences: 매콤, 따뜻한");
        assertThat(prompt).contains("Budget: 15000 KRW");
        assertThat(prompt).contains("Additional preferences: 채식주의");
        assertThat(prompt).contains("exactly 3 different Korean menu items");
        assertThat(prompt).contains("MENU: [menu name] | REASON: [brief reason]");
    }
    
    @Test
    void buildPromptHandlesNullMoods() {
        String prompt = gpt4AllClient.buildPrompt("rainy", null, 10000, null);
        
        assertThat(prompt).contains("Weather: rainy");
        assertThat(prompt).contains("Budget: 10000 KRW");
        assertThat(prompt).doesNotContain("Mood preferences:");
        assertThat(prompt).doesNotContain("Additional preferences:");
    }
    
    @Test
    void buildPromptHandlesEmptyMoods() {
        String prompt = gpt4AllClient.buildPrompt("cloudy", List.of(), 20000, "");
        
        assertThat(prompt).contains("Weather: cloudy");
        assertThat(prompt).contains("Budget: 20000 KRW");
        assertThat(prompt).doesNotContain("Mood preferences:");
        assertThat(prompt).doesNotContain("Additional preferences:");
    }
    
    @Test
    void parseMenuRecommendationsHandlesValidResponse() {
        String response = """
                {
                    "choices": [{
                        "text": "MENU: 김치찌개 | REASON: 매콤한 맛이 추운 날씨에 잘 어울립니다\\nMENU: 비빔밥 | REASON: 다양한 채소로 균형 잡힌 영양을 제공합니다\\nMENU: 불고기 | REASON: 따뜻하고 달콤한 맛으로 기분을 좋게 합니다"
                    }]
                }
                """;
        
        List<LlmClient.MenuRecommendation> recommendations = gpt4AllClient.parseMenuRecommendations(response);
        
        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).menuName()).isEqualTo("김치찌개");
        assertThat(recommendations.get(0).reason()).isEqualTo("매콤한 맛이 추운 날씨에 잘 어울립니다");
        assertThat(recommendations.get(1).menuName()).isEqualTo("비빔밥");
        assertThat(recommendations.get(2).menuName()).isEqualTo("불고기");
    }
    
    @Test
    void parseMenuRecommendationsHandlesInvalidFormat() {
        String response = """
                {
                    "choices": [{
                        "text": "이것은 잘못된 형식입니다\\n김치찌개가 좋습니다\\n그냥 텍스트"
                    }]
                }
                """;
        
        List<LlmClient.MenuRecommendation> recommendations = gpt4AllClient.parseMenuRecommendations(response);
        
        assertThat(recommendations).isEmpty();
    }
    
    @Test
    void parseMenuRecommendationsHandlesEmptyResponse() {
        String response = """
                {
                    "choices": [{
                        "text": ""
                    }]
                }
                """;
        
        List<LlmClient.MenuRecommendation> recommendations = gpt4AllClient.parseMenuRecommendations(response);
        
        assertThat(recommendations).isEmpty();
    }
    
    @Test
    void parseMenuRecommendationsHandlesMalformedJson() {
        String response = "{ invalid json }";
        
        List<LlmClient.MenuRecommendation> recommendations = gpt4AllClient.parseMenuRecommendations(response);
        
        assertThat(recommendations).isEmpty();
    }
    
    @Test
    void parseMenuRecommendationsLimitsToThree() {
        String response = """
                {
                    "choices": [{
                        "text": "MENU: 김치찌개 | REASON: 좋음\\nMENU: 비빔밥 | REASON: 좋음\\nMENU: 불고기 | REASON: 좋음\\nMENU: 냉면 | REASON: 좋음\\nMENU: 갈비탕 | REASON: 좋음"
                    }]
                }
                """;
        
        List<LlmClient.MenuRecommendation> recommendations = gpt4AllClient.parseMenuRecommendations(response);
        
        assertThat(recommendations).hasSize(3);
    }
}