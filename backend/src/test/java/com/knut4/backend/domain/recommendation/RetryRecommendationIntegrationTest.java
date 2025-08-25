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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RetryRecommendationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("이전 history 기반 재추천 성공")
    void retryLatest() throws Exception {
        var signup = new SignUpRequest("retryUser","pass12345","1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());
        var login = new LoginRequest("retryUser","pass12345");
        String loginJson = mockMvc.perform(post("/api/public/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginJson).get("accessToken").asText();
        // initial recommendation
        RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 12000, 37.11, 126.91);
        mockMvc.perform(post("/api/private/recommendations").header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        // retry without id
        mockMvc.perform(post("/api/private/recommendations/retry").header("Authorization","Bearer "+token))
                .andExpect(status().isOk());
    }
}
