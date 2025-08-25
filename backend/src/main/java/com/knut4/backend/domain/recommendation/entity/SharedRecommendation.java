package com.knut4.backend.domain.recommendation.entity;

import com.knut4.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_recommendation")
@Getter
@Setter
public class SharedRecommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private RecommendationHistory history;

    @Column(unique = true, nullable = false, length = 64)
    private String token; // UUID string

    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (token == null) token = UUID.randomUUID().toString();
    }
}
