package com.knut4.backend.domain.auth.controller;

import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.auth.dto.UserResponse;
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
    private final PasswordEncoder passwordEncoder; // for simple login pre-JWT

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        // Temporary: always 200 OK (JWT to be implemented later Issue #3)
        return ResponseEntity.ok("LOGIN_OK_PENDING_JWT");
    }
}
