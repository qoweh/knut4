package com.knut4.backend1.controller;

import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.PreferenceRequest;
import com.knut4.backend1.dto.PreferenceResponse;
import com.knut4.backend1.service.PreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PreferenceControllerTest {
    
    @Mock
    private PreferenceService preferenceService;
    
    @InjectMocks
    private PreferenceController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void getPreferences_ExistingPreference_ShouldReturnOk() {
        // Given
        PreferenceResponse mockResponse = new PreferenceResponse(
                1L,
                Arrays.asList("peanuts", "shellfish"),
                Arrays.asList("mushrooms", "olives"),
                LocalDateTime.now()
        );
        when(preferenceService.getPreferenceByUser(any(User.class))).thenReturn(Optional.of(mockResponse));
        
        // When
        ResponseEntity<PreferenceResponse> response = controller.getPreferences();
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("peanuts", response.getBody().getAllergies().get(0));
        assertEquals("shellfish", response.getBody().getAllergies().get(1));
        assertEquals("mushrooms", response.getBody().getDislikes().get(0));
        assertEquals("olives", response.getBody().getDislikes().get(1));
    }
    
    @Test
    void getPreferences_NoPreference_ShouldReturnNotFound() {
        // Given
        when(preferenceService.getPreferenceByUser(any(User.class))).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<PreferenceResponse> response = controller.getPreferences();
        
        // Then
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
    
    @Test
    void savePreferences_ValidRequest_ShouldReturnOk() {
        // Given
        PreferenceRequest request = new PreferenceRequest(
                Arrays.asList("peanuts", "shellfish"),
                Arrays.asList("mushrooms", "olives")
        );
        
        PreferenceResponse mockResponse = new PreferenceResponse(
                1L,
                request.getAllergies(),
                request.getDislikes(),
                LocalDateTime.now()
        );
        
        when(preferenceService.saveOrUpdatePreference(any(User.class), any(PreferenceRequest.class))).thenReturn(mockResponse);
        
        // When
        ResponseEntity<PreferenceResponse> response = controller.savePreferences(request);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("peanuts", response.getBody().getAllergies().get(0));
        assertEquals("shellfish", response.getBody().getAllergies().get(1));
        assertEquals("mushrooms", response.getBody().getDislikes().get(0));
        assertEquals("olives", response.getBody().getDislikes().get(1));
    }
    
    @Test
    void updatePreferences_ValidRequest_ShouldReturnOk() {
        // Given
        PreferenceRequest request = new PreferenceRequest(
                Arrays.asList("eggs", "dairy"),
                Arrays.asList("broccoli")
        );
        
        PreferenceResponse mockResponse = new PreferenceResponse(
                1L,
                request.getAllergies(),
                request.getDislikes(),
                LocalDateTime.now()
        );
        
        when(preferenceService.saveOrUpdatePreference(any(User.class), any(PreferenceRequest.class))).thenReturn(mockResponse);
        
        // When
        ResponseEntity<PreferenceResponse> response = controller.updatePreferences(request);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("eggs", response.getBody().getAllergies().get(0));
        assertEquals("dairy", response.getBody().getAllergies().get(1));
        assertEquals("broccoli", response.getBody().getDislikes().get(0));
    }
}