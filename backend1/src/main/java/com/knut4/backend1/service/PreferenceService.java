package com.knut4.backend1.service;

import com.knut4.backend1.domain.Preference;
import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.PreferenceRequest;
import com.knut4.backend1.dto.PreferenceResponse;
import com.knut4.backend1.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PreferenceService {
    
    private final PreferenceRepository preferenceRepository;
    
    public PreferenceResponse saveOrUpdatePreference(User user, PreferenceRequest request) {
        Optional<Preference> existingPreference = preferenceRepository.findByUser(user);
        
        Preference preference;
        if (existingPreference.isPresent()) {
            preference = existingPreference.get();
            preference.setAllergies(request.getAllergies() != null ? new ArrayList<>(request.getAllergies()) : new ArrayList<>());
            preference.setDislikes(request.getDislikes() != null ? new ArrayList<>(request.getDislikes()) : new ArrayList<>());
        } else {
            preference = new Preference(user, request.getAllergies(), request.getDislikes());
        }
        
        preference = preferenceRepository.save(preference);
        return mapToResponse(preference);
    }
    
    @Transactional(readOnly = true)
    public Optional<PreferenceResponse> getPreferenceByUser(User user) {
        return preferenceRepository.findByUser(user)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Optional<Preference> getPreferenceEntityByUser(User user) {
        return preferenceRepository.findByUser(user);
    }
    
    private PreferenceResponse mapToResponse(Preference preference) {
        return new PreferenceResponse(
                preference.getId(),
                preference.getAllergies(),
                preference.getDislikes(),
                preference.getCreatedAt()
        );
    }
}