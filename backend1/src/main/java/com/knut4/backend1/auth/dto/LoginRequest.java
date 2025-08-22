package com.knut4.backend1.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 로그인 요청 DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}