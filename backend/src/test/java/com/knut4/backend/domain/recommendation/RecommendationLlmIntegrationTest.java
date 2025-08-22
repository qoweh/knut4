package com.knut4.backend.domain.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.AccessTokenResponse;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-llm-enabled")
class RecommendationLlmIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create user and get access token for authentication
        var signup = new SignUpRequest("testuser", "password123", "1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        var login = new LoginRequest("testuser", "password123");
        String loginJson = mockMvc.perform(post("/api/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        AccessTokenResponse resp = objectMapper.readValue(loginJson, AccessTokenResponse.class);
        this.accessToken = resp.accessToken();
    }

    @Test
    @DisplayName("LLM 활성화시 추천 API 호출 - 타임아웃 시 fallback 동작")
    void recommendWithLlmTimeoutFallback() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
                "sunny", 
                List.of("매콤", "따뜻한"), 
                15000, 
                37.5665, 
                126.9780
        );

        mockMvc.perform(post("/api/private/recommendations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuRecommendations").isArray())
                .andExpect(jsonPath("$.menuRecommendations").isNotEmpty())
                // Should fallback to first mood when LLM fails (due to non-existent URL)
                .andExpect(jsonPath("$.menuRecommendations[0].menuName").value("매콤"))
                .andExpect(jsonPath("$.menuRecommendations[0].reason").exists());
    }
}