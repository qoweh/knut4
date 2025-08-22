package com.knut4.backend1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.RecommendationHistoryResponse;
import com.knut4.backend1.service.RecommendationHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RecommendationHistoryControllerTest {
    
    @Mock
    private RecommendationHistoryService recommendationHistoryService;
    
    @InjectMocks
    private RecommendationHistoryController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void getRecommendationHistory_WithoutPagination_ShouldReturnAllHistory() {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Arrays.asList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now()),
                new RecommendationHistoryResponse(2L, Arrays.asList("pasta", "salad"), "dinner", LocalDateTime.now())
        );
        
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class))).thenReturn(mockHistory);
        
        // When
        ResponseEntity<List<RecommendationHistoryResponse>> response = controller.getRecommendationHistory(0, 0);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("chicken", response.getBody().get(0).getRecommendedItems().get(0));
        assertEquals("lunch", response.getBody().get(0).getContext());
    }
    
    @Test
    void getRecommendationHistory_WithPagination_ShouldReturnPagedResults() {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Collections.singletonList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now())
        );
        
        Page<RecommendationHistoryResponse> mockPage = new PageImpl<>(mockHistory, PageRequest.of(0, 10), 1);
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class), eq(PageRequest.of(0, 10))))
                .thenReturn(mockPage);
        
        // When
        ResponseEntity<List<RecommendationHistoryResponse>> response = controller.getRecommendationHistory(0, 10);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("chicken", response.getBody().get(0).getRecommendedItems().get(0));
    }
    
    @Test
    void getRecommendationHistory_EmptyHistory_ShouldReturnEmptyArray() {
        // Given
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class)))
                .thenReturn(Collections.emptyList());
        
        // When
        ResponseEntity<List<RecommendationHistoryResponse>> response = controller.getRecommendationHistory(0, 0);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }
    
    @Test
    void getRecommendationHistory_InvalidPageSize_ShouldFallBackToNonPaged() {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Arrays.asList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now())
        );
        
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class))).thenReturn(mockHistory);
        
        // When - negative page size should trigger non-paged query
        ResponseEntity<List<RecommendationHistoryResponse>> response = controller.getRecommendationHistory(0, -1);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}