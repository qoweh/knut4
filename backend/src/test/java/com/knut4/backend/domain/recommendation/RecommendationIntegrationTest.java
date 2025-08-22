package com.knut4.backend.domain.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.AccessTokenResponse;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 → 로그인 → 추천 → 재추천 플로우")
    void signup_login_recommend_retry_flow() throws Exception {
        // 1. 회원가입
        var signup = new SignUpRequest("testuser", "password123", "1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        // 2. 로그인
        var login = new LoginRequest("testuser", "password123");
        String loginJson = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AccessTokenResponse loginResp = objectMapper.readValue(loginJson, AccessTokenResponse.class);
        String token = loginResp.accessToken();

        // 3. 추천 요청
        var recommendRequest = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.5665, 126.9780);
        String recommendJson = mockMvc.perform(post("/api/private/recommendations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recommendRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        RecommendationResponse recommendResp = objectMapper.readValue(recommendJson, RecommendationResponse.class);
        assertThat(recommendResp.menuRecommendations()).hasSizeGreaterThanOrEqualTo(1);

        // 4. 재추천 요청 (historyId 없이)
        String retryJson = mockMvc.perform(post("/api/private/recommendations/retry")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        RecommendationResponse retryResp = objectMapper.readValue(retryJson, RecommendationResponse.class);
        assertThat(retryResp.menuRecommendations()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("잘못된 historyId로 재추천 요청시 404")
    void retry_with_invalid_historyId_returns_404() throws Exception {
        // 1. 회원가입 및 로그인
        var signup = new SignUpRequest("testuser2", "password123", "1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        var login = new LoginRequest("testuser2", "password123");
        String loginJson = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AccessTokenResponse loginResp = objectMapper.readValue(loginJson, AccessTokenResponse.class);
        String token = loginResp.accessToken();

        // 2. 존재하지 않는 historyId로 재추천 요청
        mockMvc.perform(post("/api/private/recommendations/retry?historyId=999999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("추천 기록 없이 재추천 요청시 404")
    void retry_without_history_returns_404() throws Exception {
        // 1. 회원가입 및 로그인 (추천 기록 없음)
        var signup = new SignUpRequest("testuser3", "password123", "1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        var login = new LoginRequest("testuser3", "password123");
        String loginJson = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AccessTokenResponse loginResp = objectMapper.readValue(loginJson, AccessTokenResponse.class);
        String token = loginResp.accessToken();

        // 2. 추천 기록 없이 재추천 요청
        mockMvc.perform(post("/api/private/recommendations/retry")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}