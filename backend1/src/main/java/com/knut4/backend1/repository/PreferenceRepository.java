package com.knut4.backend1.repository;

import com.knut4.backend1.domain.Preference;
import com.knut4.backend1.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    
    Optional<Preference> findByUser(User user);
    
    Optional<Preference> findByUserId(Long userId);
    
    void deleteByUser(User user);
}