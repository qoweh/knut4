package com.knut4.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignUpRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String birthDate
) {
    public LocalDate birthDateAsLocalDate() {
        return birthDate == null || birthDate.isBlank() ? null : LocalDate.parse(birthDate);
    }
}
