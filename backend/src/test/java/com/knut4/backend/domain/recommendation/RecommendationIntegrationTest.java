package com.knut4.backend.domain.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("인증 없이 추천 요청시 401")
    void recommendUnauthorizedWithoutToken() throws Exception {
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
        mockMvc.perform(post("/api/private/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원가입/로그인 후 추천 200")
    void recommendAfterAuth() throws Exception {
        // signup
        SignUpRequest su = new SignUpRequest("user1234", "password123", "1999-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(su)))
                .andExpect(status().isOk());
        // login
        LoginRequest lr = new LoginRequest("user1234", "password123");
        String tokenJson = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lr)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(tokenJson).get("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
        mockMvc.perform(post("/api/private/recommendations")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
