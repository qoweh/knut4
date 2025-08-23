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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareRecommendationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("추천 결과 공유 후 토큰으로 공개 조회")
    void shareAndFetch() throws Exception {
        var su = new SignUpRequest("shareUser","pass12345","1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(su)))
                .andExpect(status().isOk());
        var lr = new LoginRequest("shareUser","pass12345");
        String loginJson = mockMvc.perform(post("/api/public/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(lr)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginJson).get("accessToken").asText();
        var req = new RecommendationRequest("sunny", List.of("달달"), 15000, 37.2, 126.95);
        mockMvc.perform(post("/api/private/recommendations").header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        String shareJson = mockMvc.perform(post("/api/private/recommendations/share").header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String shareToken = objectMapper.readTree(shareJson).get("token").asText();
        assertThat(shareToken).isNotBlank();
        mockMvc.perform(get("/api/public/recommendations/shared/"+shareToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(shareToken));
    }
}
