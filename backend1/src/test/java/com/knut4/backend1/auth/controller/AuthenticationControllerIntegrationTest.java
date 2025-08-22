package com.knut4.backend1.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend1.auth.dto.LoginRequest;
import com.knut4.backend1.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 인증 컨트롤러 통합 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
class AuthenticationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("유효한 로그인 요청 시 JWT 토큰 반환")
    void login_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    @DisplayName("잘못된 로그인 요청 시 401 Unauthorized 반환")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpass");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 요청 데이터 시 400 Bad Request 반환")
    void login_WithInvalidRequestData_ShouldReturnBadRequest() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(""); // 빈 username
        loginRequest.setPassword("testpass");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증이 필요한 엔드포인트에 토큰 없이 접근 시 403 반환")
    void accessProtectedEndpoint_WithoutToken_ShouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증이 필요한 엔드포인트에 유효한 토큰으로 접근 시 성공")
    void accessProtectedEndpoint_WithValidToken_ShouldReturnSuccess() throws Exception {
        // given
        String token = jwtTokenProvider.generateToken("testuser");

        // when & then
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a protected endpoint"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 접근 시 403 반환")
    void accessProtectedEndpoint_WithInvalidToken_ShouldReturnForbidden() throws Exception {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }
}