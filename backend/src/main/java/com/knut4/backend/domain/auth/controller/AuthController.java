package com.knut4.backend.domain.auth.controller;

import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.AccessTokenResponse;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.auth.dto.UserResponse;
import com.knut4.backend.common.security.JwtTokenProvider;
import com.knut4.backend.domain.auth.service.UserService;
import com.knut4.backend.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.username());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("잘못된 자격 증명입니다.");
        }
    var token = tokenProvider.createAccessToken(user.getUsername());
    var expiresAt = tokenProvider.getExpirationInstant(token);
    return ResponseEntity.ok(new AccessTokenResponse(token, expiresAt));
    }
}
