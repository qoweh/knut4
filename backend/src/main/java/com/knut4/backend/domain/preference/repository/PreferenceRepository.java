package com.knut4.backend.domain.preference.repository;

import com.knut4.backend.domain.preference.entity.Preference;
import com.knut4.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    Optional<Preference> findByUser(User user);
}
