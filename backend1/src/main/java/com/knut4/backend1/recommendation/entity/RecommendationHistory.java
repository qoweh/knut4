package com.knut4.backend1.recommendation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to store recommendation history
 */
@Entity
@Table(name = "recommendation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // nullable - for anonymous users
    
    @Column(name = "weather")
    private String weather;
    
    @Column(name = "moods", columnDefinition = "TEXT")
    private String moods; // JSON string of mood array
    
    @Column(name = "budget")
    private Integer budget;
    
    @Column(name = "lat", nullable = false)
    private Double lat;
    
    @Column(name = "lon", nullable = false)
    private Double lon;
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations; // JSON string of recommendation response
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}