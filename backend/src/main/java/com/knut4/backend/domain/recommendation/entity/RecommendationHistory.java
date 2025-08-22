package com.knut4.backend.domain.recommendation.entity;

import com.knut4.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "recommendation_history")
@Getter
@Setter
public class RecommendationHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // nullable for anonymous extension

    private String weather;
    private String moods; // comma separated
    private Integer budget;
    private Double latitude;
    private Double longitude;
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
