package com.knut4.backend.domain.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HistoryIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        SignUpRequest signUp = new SignUpRequest("histUser", "password123", "1990-01-01");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isOk());
        var loginResp = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("histUser", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        String body = loginResp.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    void historyReturnsUserScopedEntries() throws Exception {
        RecommendationRequest req = new RecommendationRequest("맑음", List.of("든든"), 10000, 37.0, 127.0);
        mockMvc.perform(post("/api/private/recommendations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/private/history")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
