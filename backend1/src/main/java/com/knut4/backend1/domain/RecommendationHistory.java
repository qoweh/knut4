package com.knut4.backend1.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation_history")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecommendationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ElementCollection
    @CollectionTable(name = "recommendation_items", joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "item")
    private List<String> recommendedItems = new ArrayList<>();
    
    @Column(name = "recommendation_context")
    private String context;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    public RecommendationHistory(User user, List<String> recommendedItems, String context) {
        this.user = user;
        this.recommendedItems = recommendedItems != null ? new ArrayList<>(recommendedItems) : new ArrayList<>();
        this.context = context;
    }
}