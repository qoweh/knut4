package com.knut4.backend1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.PreferenceRequest;
import com.knut4.backend1.dto.PreferenceResponse;
import com.knut4.backend1.service.PreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PreferenceController.class)
class PreferenceControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PreferenceService preferenceService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(username = "testuser")
    void getPreferences_ExistingPreference_ShouldReturnOk() throws Exception {
        // Given
        PreferenceResponse mockResponse = new PreferenceResponse(
                1L,
                Arrays.asList("peanuts", "shellfish"),
                Arrays.asList("mushrooms", "olives"),
                LocalDateTime.now()
        );
        when(preferenceService.getPreferenceByUser(any(User.class))).thenReturn(Optional.of(mockResponse));
        
        // When & Then
        mockMvc.perform(get("/api/me/preferences"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.allergies[0]").value("peanuts"))
                .andExpect(jsonPath("$.allergies[1]").value("shellfish"))
                .andExpect(jsonPath("$.dislikes[0]").value("mushrooms"))
                .andExpect(jsonPath("$.dislikes[1]").value("olives"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void getPreferences_NoPreference_ShouldReturnNotFound() throws Exception {
        // Given
        when(preferenceService.getPreferenceByUser(any(User.class))).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/me/preferences"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void savePreferences_ValidRequest_ShouldReturnOk() throws Exception {
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
        
        when(preferenceService.saveOrUpdatePreference(any(User.class), argThat(req -> 
                req.getAllergies().equals(request.getAllergies()) && 
                req.getDislikes().equals(request.getDislikes())
        ))).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(post("/api/me/preferences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.allergies[0]").value("peanuts"))
                .andExpect(jsonPath("$.allergies[1]").value("shellfish"))
                .andExpect(jsonPath("$.dislikes[0]").value("mushrooms"))
                .andExpect(jsonPath("$.dislikes[1]").value("olives"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void savePreferences_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with null allergies (violates @NotNull validation)
        String invalidRequestJson = "{\"allergies\":null,\"dislikes\":[\"mushrooms\"]}";
        
        // When & Then
        mockMvc.perform(post("/api/me/preferences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void updatePreferences_ValidRequest_ShouldReturnOk() throws Exception {
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
        
        when(preferenceService.saveOrUpdatePreference(any(User.class), argThat(req -> 
                req.getAllergies().equals(request.getAllergies()) && 
                req.getDislikes().equals(request.getDislikes())
        ))).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(put("/api/me/preferences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.allergies[0]").value("eggs"))
                .andExpect(jsonPath("$.allergies[1]").value("dairy"))
                .andExpect(jsonPath("$.dislikes[0]").value("broccoli"));
    }
}