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
@Table(name = "preferences")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Preference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ElementCollection
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "allergy")
    private List<String> allergies = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "user_dislikes", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "dislike")
    private List<String> dislikes = new ArrayList<>();
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    public Preference(User user) {
        this.user = user;
    }
    
    public Preference(User user, List<String> allergies, List<String> dislikes) {
        this.user = user;
        this.allergies = allergies != null ? new ArrayList<>(allergies) : new ArrayList<>();
        this.dislikes = dislikes != null ? new ArrayList<>(dislikes) : new ArrayList<>();
    }
}