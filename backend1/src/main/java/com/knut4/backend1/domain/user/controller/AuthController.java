package com.knut4.backend1.domain.user.controller;

import com.knut4.backend1.domain.user.dto.LoginRequest;
import com.knut4.backend1.domain.user.dto.SignUpRequest;
import com.knut4.backend1.domain.user.dto.UserResponse;
import com.knut4.backend1.domain.user.exception.AuthenticationFailedException;
import com.knut4.backend1.domain.user.exception.DuplicateUserException;
import com.knut4.backend1.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            UserResponse userResponse = userService.signUp(signUpRequest);
            return ResponseEntity.ok(userResponse);
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            boolean isValid = userService.verifyPassword(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
            );
            
            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Login successful"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "Invalid username or password"));
            }
        } catch (AuthenticationFailedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}