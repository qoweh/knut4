package com.knut4.backend1.domain.user.service;

import com.knut4.backend1.domain.user.dto.SignUpRequest;
import com.knut4.backend1.domain.user.dto.UserResponse;
import com.knut4.backend1.domain.user.entity.User;
import com.knut4.backend1.domain.user.exception.AuthenticationFailedException;
import com.knut4.backend1.domain.user.exception.DuplicateUserException;
import com.knut4.backend1.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private SignUpRequest signUpRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("testuser", "password123", LocalDate.of(1990, 1, 1));
        
        user = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedPassword")
                .birthDate(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("회원가입 성공 - 새로운 사용자")
    void signUp_Success_NewUser() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        UserResponse result = userService.signUp(signUpRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getCreatedAt()).isNotNull();
        
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 중복된 사용자명")
    void signUp_Failure_DuplicateUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.signUp(signUpRequest))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("User with username 'testuser' already exists");
        
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("패스워드 검증 성공 - 올바른 패스워드")
    void verifyPassword_Success_CorrectPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        
        // When
        boolean result = userService.verifyPassword("testuser", "password123");
        
        // Then
        assertThat(result).isTrue();
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "hashedPassword");
    }
    
    @Test
    @DisplayName("패스워드 검증 실패 - 잘못된 패스워드")
    void verifyPassword_Failure_IncorrectPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);
        
        // When
        boolean result = userService.verifyPassword("testuser", "wrongpassword");
        
        // Then
        assertThat(result).isFalse();
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "hashedPassword");
    }
    
    @Test
    @DisplayName("패스워드 검증 실패 - 존재하지 않는 사용자")
    void verifyPassword_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.verifyPassword("nonexistentuser", "password123"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");
        
        verify(userRepository).findByUsername("nonexistentuser");
        verify(passwordEncoder, never()).matches(any(), any());
    }
}