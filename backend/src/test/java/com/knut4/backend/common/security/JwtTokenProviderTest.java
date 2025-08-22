package com.knut4.backend.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", "test-secret-test-secret-test-secret-test-secret-1234567890");
        ReflectionTestUtils.setField(provider, "accessValiditySeconds", 2L); // short expiry
        ReflectionTestUtils.setField(provider, "issuer", "test-issuer");
        provider.afterPropertiesSet();
    }

    @Test
    @DisplayName("토큰 생성 및 subject 검증")
    void create_and_validate() {
        String token = provider.createAccessToken("alice");
        String subject = provider.validateAndGetSubject(token);
        assertThat(subject).isEqualTo("alice");
        Instant exp = provider.getExpirationInstant(token);
        assertThat(exp).isAfter(Instant.now());
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void expired_token() throws InterruptedException {
        String token = provider.createAccessToken("bob");
        Thread.sleep(2500); // wait > 2s
        assertThatThrownBy(() -> provider.validateAndGetSubject(token))
                .isInstanceOf(Exception.class);
    }
}
