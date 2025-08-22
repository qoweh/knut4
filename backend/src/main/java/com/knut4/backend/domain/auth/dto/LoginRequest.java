package com.knut4.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password
) {}
