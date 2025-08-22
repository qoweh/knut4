package com.knut4.backend.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.auth.dto.AccessTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 후 로그인하여 토큰 수령 및 /api/private/me 접근")
    void signup_login_me_flow() throws Exception {
        var signup = new SignUpRequest("charlie","password123","1991-02-03");
        mockMvc.perform(post("/api/public/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        var login = new LoginRequest("charlie","password123");
        String loginJson = mockMvc.perform(post("/api/public/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AccessTokenResponse resp = objectMapper.readValue(loginJson, AccessTokenResponse.class);
        assertThat(resp.accessToken()).isNotBlank();
        assertThat(resp.expiresAt()).isAfter(java.time.Instant.now());

        mockMvc.perform(get("/api/private/me").header("Authorization","Bearer "+resp.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    @DisplayName("토큰 없이 보호 API 접근시 401")
    void unauthorized_without_token() throws Exception {
        mockMvc.perform(get("/api/private/me"))
                .andExpect(status().isUnauthorized());
    }
}
