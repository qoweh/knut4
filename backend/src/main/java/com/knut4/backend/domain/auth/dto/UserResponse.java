package com.knut4.backend.domain.auth.dto;

import com.knut4.backend.domain.user.User;

import java.time.OffsetDateTime;

public record UserResponse(Long id, String username, String birthDate, OffsetDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getBirthDate() == null ? null : user.getBirthDate().toString(),
                user.getCreatedAt()
        );
    }
}
