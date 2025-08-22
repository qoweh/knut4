package com.knut4.backend1.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Token Provider 테스트
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "mySecretKey1234567890123456789012345678901234567890123456789012345678901234567890";
    private static final long TEST_EXPIRATION = 60000; // 1 minute for testing

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void generateToken_ShouldCreateValidToken() {
        // given
        String username = "testuser";

        // when
        String token = jwtTokenProvider.generateToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출 테스트")
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // when
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 테스트")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // given
        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(TEST_SECRET, 1); // 1ms expiration
        String username = "testuser";
        String token = shortExpiryProvider.generateToken(username);
        
        // wait for token to expire
        Thread.sleep(10);

        // when
        boolean isValid = shortExpiryProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 만료 시간 추출 테스트")
    void getExpirationFromToken_ShouldReturnCorrectExpiration() {
        // given
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // when
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // then
        assertThat(expiration).isNotNull();
        
        // Check that expiration is in the future and roughly correct (within 1 second tolerance)
        long now = System.currentTimeMillis();
        long expectedExpiration = now + TEST_EXPIRATION;
        long tolerance = 2000; // 2 seconds tolerance
        
        assertThat(expiration.getTime()).isBetween(expectedExpiration - tolerance, expectedExpiration + tolerance);
    }

    @Test
    @DisplayName("빈 토큰 검증 테스트")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // when & then
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰에서 사용자명 추출 시 예외 발생")
    void getUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }
}