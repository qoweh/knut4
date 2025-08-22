package com.knut4.backend1.domain.user.service;

import com.knut4.backend1.domain.user.dto.SignUpRequest;
import com.knut4.backend1.domain.user.dto.UserResponse;
import com.knut4.backend1.domain.user.entity.User;
import com.knut4.backend1.domain.user.exception.AuthenticationFailedException;
import com.knut4.backend1.domain.user.exception.DuplicateUserException;
import com.knut4.backend1.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserResponse signUp(SignUpRequest signUpRequest) {
        // Check for duplicate username
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new DuplicateUserException(signUpRequest.getUsername());
        }
        
        // Create user entity
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .birthDate(signUpRequest.getBirthDate())
                .createdAt(LocalDateTime.now())
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Convert to response DTO
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .birthDate(savedUser.getBirthDate())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
    
    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));
        
        return passwordEncoder.matches(password, user.getPasswordHash());
    }
}