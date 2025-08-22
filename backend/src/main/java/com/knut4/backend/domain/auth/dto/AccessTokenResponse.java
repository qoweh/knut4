package com.knut4.backend.domain.auth.dto;

import java.time.Instant;

public record AccessTokenResponse(String accessToken, Instant expiresAt) {}
