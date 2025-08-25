package com.knut4.backend.domain.preference.controller;

import com.knut4.backend.domain.preference.entity.Preference;
import com.knut4.backend.domain.preference.repository.PreferenceRepository;
import com.knut4.backend.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/private/preferences")
@RequiredArgsConstructor
public class PreferenceController {
    private final PreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<PreferenceDto> get(Authentication auth) {
        var pref = currentPreference(auth);
        return ResponseEntity.ok(pref.map(PreferenceDto::from).orElse(null));
    }

    @PostMapping
    public ResponseEntity<PreferenceDto> upsert(@RequestBody PreferenceDto dto, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User u)) {
            return ResponseEntity.status(401).build();
        }
        var user = userRepository.findByUsername(u.getUsername()).orElseThrow();
        Preference p = preferenceRepository.findByUser(user).orElseGet(() -> { Preference np = new Preference(); np.setUser(user); return np; });
        p.setLikes(dto.likes());
        p.setDislikes(dto.dislikes());
        p.setAllergies(dto.allergies());
        p.setDietTypes(dto.dietTypes());
        p.setNotes(dto.notes());
        preferenceRepository.save(p);
        return ResponseEntity.ok(PreferenceDto.from(p));
    }

    private Optional<Preference> currentPreference(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User u)) return Optional.empty();
        return userRepository.findByUsername(u.getUsername()).flatMap(preferenceRepository::findByUser);
    }
}

record PreferenceDto(Long id, String likes, String dislikes, String allergies, String dietTypes, String notes) {
    static PreferenceDto from(Preference p) { return new PreferenceDto(p.getId(), p.getLikes(), p.getDislikes(), p.getAllergies(), p.getDietTypes(), p.getNotes()); }
}
