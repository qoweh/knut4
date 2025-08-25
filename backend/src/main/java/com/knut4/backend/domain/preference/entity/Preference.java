package com.knut4.backend.domain.preference.entity;

import com.knut4.backend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "preferences", indexes = {
        @Index(name = "idx_pref_user", columnList = "user_id", unique = true)
})
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 500)
    private String likes; // comma separated liked ingredients/menus

    @Column(length = 500)
    private String dislikes; // comma separated

    @Column(length = 500)
    private String allergies; // comma separated

    @Column(length = 500)
    private String dietTypes; // e.g., vegan, keto

    @Column(length = 1000)
    private String notes; // free form

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLikes() { return likes; }
    public void setLikes(String likes) { this.likes = likes; }
    public String getDislikes() { return dislikes; }
    public void setDislikes(String dislikes) { this.dislikes = dislikes; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getDietTypes() { return dietTypes; }
    public void setDietTypes(String dietTypes) { this.dietTypes = dietTypes; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // helper split methods returning arrays (empty safe)
    public String[] dislikeArray() { return split(dislikes); }
    public String[] allergyArray() { return split(allergies); }

    private String[] split(String s) {
        if (s == null || s.isBlank()) return new String[0];
        return java.util.Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .toArray(String[]::new);
    }
}
