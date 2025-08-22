package com.knut4.backend1.service;

import com.knut4.backend1.domain.Preference;
import com.knut4.backend1.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationFilterServiceTest {
    
    @Mock
    private PreferenceService preferenceService;
    
    @InjectMocks
    private RecommendationFilterService recommendationFilterService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
    }
    
    @Test
    void filterRecommendations_NoPreferences_ShouldReturnOriginalList() {
        // Given
        List<String> originalRecommendations = Arrays.asList("chicken rice", "beef noodles", "fish soup");
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.empty());
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(originalRecommendations, filtered);
    }
    
    @Test
    void filterRecommendations_WithAllergies_ShouldFilterOut() {
        // Given
        List<String> allergies = Arrays.asList("peanuts", "shellfish");
        List<String> dislikes = Collections.emptyList();
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList(
                "chicken rice", 
                "peanut butter sandwich", 
                "shellfish pasta", 
                "beef noodles"
        );
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("chicken rice"));
        assertTrue(filtered.contains("beef noodles"));
    }
    
    @Test
    void filterRecommendations_WithDislikes_ShouldFilterOut() {
        // Given
        List<String> allergies = Collections.emptyList();
        List<String> dislikes = Arrays.asList("mushrooms", "olives");
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList(
                "chicken rice", 
                "mushroom pasta", 
                "olives pizza", 
                "beef noodles"
        );
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("chicken rice"));
        assertTrue(filtered.contains("beef noodles"));
    }
    
    @Test
    void filterRecommendations_WithBothAllergiesAndDislikes_ShouldFilterBoth() {
        // Given
        List<String> allergies = Arrays.asList("peanuts");
        List<String> dislikes = Arrays.asList("mushrooms");
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList(
                "chicken rice", 
                "peanut butter sandwich", 
                "mushroom pasta", 
                "beef noodles"
        );
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("chicken rice"));
        assertTrue(filtered.contains("beef noodles"));
    }
    
    @Test
    void filterRecommendations_CaseInsensitive_ShouldFilterCorrectly() {
        // Given
        List<String> allergies = Arrays.asList("PEANUTS");
        List<String> dislikes = Arrays.asList("MUSHROOMS");
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList(
                "chicken rice", 
                "peanut butter sandwich", 
                "Mushroom pasta", 
                "beef noodles"
        );
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("chicken rice"));
        assertTrue(filtered.contains("beef noodles"));
    }
    
    @Test
    void filterRecommendations_EmptyLists_ShouldReturnOriginal() {
        // Given
        List<String> allergies = Collections.emptyList();
        List<String> dislikes = Collections.emptyList();
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList("chicken rice", "beef noodles");
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(originalRecommendations, filtered);
    }
    
    @Test
    void filterRecommendations_NullValues_ShouldHandleGracefully() {
        // Given
        List<String> allergies = Arrays.asList("peanuts", null, "");
        List<String> dislikes = Arrays.asList(null, "mushrooms", "");
        Preference preference = new Preference(testUser, allergies, dislikes);
        
        List<String> originalRecommendations = Arrays.asList(
                "chicken rice", 
                "peanut butter sandwich", 
                "mushroom pasta"
        );
        
        when(preferenceService.getPreferenceEntityByUser(testUser)).thenReturn(Optional.of(preference));
        
        // When
        List<String> filtered = recommendationFilterService.filterRecommendations(testUser, originalRecommendations);
        
        // Then
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains("chicken rice"));
    }
}