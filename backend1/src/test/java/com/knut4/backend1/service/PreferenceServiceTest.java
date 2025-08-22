package com.knut4.backend1.service;

import com.knut4.backend1.domain.Preference;
import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.PreferenceRequest;
import com.knut4.backend1.dto.PreferenceResponse;
import com.knut4.backend1.repository.PreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {
    
    @Mock
    private PreferenceRepository preferenceRepository;
    
    @InjectMocks
    private PreferenceService preferenceService;
    
    private User testUser;
    private Preference testPreference;
    private PreferenceRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        List<String> allergies = Arrays.asList("peanuts", "shellfish");
        List<String> dislikes = Arrays.asList("mushrooms", "olives");
        
        testPreference = new Preference(testUser, allergies, dislikes);
        testPreference.setId(1L);
        testPreference.setCreatedAt(LocalDateTime.now());
        
        testRequest = new PreferenceRequest(allergies, dislikes);
    }
    
    @Test
    void saveOrUpdatePreference_NewPreference_ShouldCreateNew() {
        // Given
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(Preference.class))).thenReturn(testPreference);
        
        // When
        PreferenceResponse response = preferenceService.saveOrUpdatePreference(testUser, testRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(testPreference.getId(), response.getId());
        assertEquals(testPreference.getAllergies(), response.getAllergies());
        assertEquals(testPreference.getDislikes(), response.getDislikes());
        assertEquals(testPreference.getCreatedAt(), response.getCreatedAt());
        
        verify(preferenceRepository).findByUser(testUser);
        verify(preferenceRepository).save(any(Preference.class));
    }
    
    @Test
    void saveOrUpdatePreference_ExistingPreference_ShouldUpdate() {
        // Given
        List<String> newAllergies = Arrays.asList("eggs", "dairy");
        List<String> newDislikes = Arrays.asList("broccoli");
        PreferenceRequest updateRequest = new PreferenceRequest(newAllergies, newDislikes);
        
        Preference existingPreference = new Preference(testUser, Arrays.asList("peanuts"), Arrays.asList("mushrooms"));
        existingPreference.setId(1L);
        existingPreference.setCreatedAt(LocalDateTime.now());
        
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(existingPreference));
        when(preferenceRepository.save(any(Preference.class))).thenReturn(existingPreference);
        
        // When
        PreferenceResponse response = preferenceService.saveOrUpdatePreference(testUser, updateRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(newAllergies, existingPreference.getAllergies());
        assertEquals(newDislikes, existingPreference.getDislikes());
        
        verify(preferenceRepository).findByUser(testUser);
        verify(preferenceRepository).save(existingPreference);
    }
    
    @Test
    void getPreferenceByUser_ExistingPreference_ShouldReturnPreference() {
        // Given
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(testPreference));
        
        // When
        Optional<PreferenceResponse> response = preferenceService.getPreferenceByUser(testUser);
        
        // Then
        assertTrue(response.isPresent());
        assertEquals(testPreference.getId(), response.get().getId());
        assertEquals(testPreference.getAllergies(), response.get().getAllergies());
        assertEquals(testPreference.getDislikes(), response.get().getDislikes());
        
        verify(preferenceRepository).findByUser(testUser);
    }
    
    @Test
    void getPreferenceByUser_NoPreference_ShouldReturnEmpty() {
        // Given
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.empty());
        
        // When
        Optional<PreferenceResponse> response = preferenceService.getPreferenceByUser(testUser);
        
        // Then
        assertFalse(response.isPresent());
        verify(preferenceRepository).findByUser(testUser);
    }
    
    @Test
    void getPreferenceEntityByUser_ShouldReturnEntity() {
        // Given
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(testPreference));
        
        // When
        Optional<Preference> response = preferenceService.getPreferenceEntityByUser(testUser);
        
        // Then
        assertTrue(response.isPresent());
        assertEquals(testPreference, response.get());
        verify(preferenceRepository).findByUser(testUser);
    }
    
    @Test
    void saveOrUpdatePreference_WithNullLists_ShouldHandleGracefully() {
        // Given
        PreferenceRequest requestWithNulls = new PreferenceRequest(null, null);
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.empty());
        
        Preference savedPreference = new Preference(testUser);
        savedPreference.setId(1L);
        savedPreference.setCreatedAt(LocalDateTime.now());
        when(preferenceRepository.save(any(Preference.class))).thenReturn(savedPreference);
        
        // When
        PreferenceResponse response = preferenceService.saveOrUpdatePreference(testUser, requestWithNulls);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getAllergies());
        assertNotNull(response.getDislikes());
        assertTrue(response.getAllergies().isEmpty());
        assertTrue(response.getDislikes().isEmpty());
    }
}