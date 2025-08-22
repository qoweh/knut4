package com.knut4.backend1.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String username;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
}