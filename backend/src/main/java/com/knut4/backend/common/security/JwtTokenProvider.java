package com.knut4.backend.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider implements InitializingBean {

    @Value("${app.jwt.secret:dev-secret-please-override-dev-secret-please-override-dev-secret}")
    private String secret;
    @Value("${app.jwt.access-token-validity-seconds:3600}")
    private long accessValiditySeconds;
    @Value("${app.jwt.issuer:knut4}")
    private String issuer;

    private SecretKey key;

    @Override
    public void afterPropertiesSet() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessValiditySeconds);
        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Instant getExpirationInstant(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
