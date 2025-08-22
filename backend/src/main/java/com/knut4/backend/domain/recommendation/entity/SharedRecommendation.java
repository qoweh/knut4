package com.knut4.backend.domain.recommendation.entity;

import com.knut4.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_recommendation", indexes = {
        @Index(name = "uk_shared_recommendation_token", columnList = "token", unique = true)
})
@Getter
@Setter
public class SharedRecommendation {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private RecommendationHistory history;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (token == null) token = UUID.randomUUID().toString();
    }
}