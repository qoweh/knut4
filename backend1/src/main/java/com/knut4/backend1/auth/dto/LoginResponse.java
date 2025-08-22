package com.knut4.backend1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * 로그인 응답 DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private Instant expiresAt;
}