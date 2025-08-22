package com.knut4.backend1.auth.controller;

import com.knut4.backend1.auth.dto.LoginRequest;
import com.knut4.backend1.auth.dto.LoginResponse;
import com.knut4.backend1.auth.jwt.JwtTokenProvider;
import com.knut4.backend1.auth.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * 인증 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 엔드포인트
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(authentication.getName());
            Instant expiresAt = jwtTokenProvider.getExpirationFromToken(token).toInstant();

            LoginResponse response = new LoginResponse(token, expiresAt);
            
            log.info("User {} logged in successfully", loginRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            log.warn("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}