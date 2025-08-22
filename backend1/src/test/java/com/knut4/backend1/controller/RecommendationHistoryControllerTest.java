package com.knut4.backend1.controller;

import com.knut4.backend1.domain.User;
import com.knut4.backend1.dto.RecommendationHistoryResponse;
import com.knut4.backend1.service.RecommendationHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationHistoryController.class)
class RecommendationHistoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RecommendationHistoryService recommendationHistoryService;
    
    @Test
    @WithMockUser(username = "testuser")
    void getRecommendationHistory_WithoutPagination_ShouldReturnAllHistory() throws Exception {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Arrays.asList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now()),
                new RecommendationHistoryResponse(2L, Arrays.asList("pasta", "salad"), "dinner", LocalDateTime.now())
        );
        
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class))).thenReturn(mockHistory);
        
        // When & Then
        mockMvc.perform(get("/api/me/recommendations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].recommendedItems[0]").value("chicken"))
                .andExpect(jsonPath("$[0].recommendedItems[1]").value("rice"))
                .andExpect(jsonPath("$[0].context").value("lunch"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].recommendedItems[0]").value("pasta"))
                .andExpect(jsonPath("$[1].recommendedItems[1]").value("salad"))
                .andExpect(jsonPath("$[1].context").value("dinner"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void getRecommendationHistory_WithPagination_ShouldReturnPagedResults() throws Exception {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Collections.singletonList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now())
        );
        
        Page<RecommendationHistoryResponse> mockPage = new PageImpl<>(mockHistory, PageRequest.of(0, 10), 1);
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class), eq(PageRequest.of(0, 10))))
                .thenReturn(mockPage);
        
        // When & Then
        mockMvc.perform(get("/api/me/recommendations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].recommendedItems[0]").value("chicken"))
                .andExpect(jsonPath("$[0].context").value("lunch"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void getRecommendationHistory_EmptyHistory_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class)))
                .thenReturn(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(get("/api/me/recommendations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void getRecommendationHistory_InvalidPageSize_ShouldFallBackToNonPaged() throws Exception {
        // Given
        List<RecommendationHistoryResponse> mockHistory = Arrays.asList(
                new RecommendationHistoryResponse(1L, Arrays.asList("chicken", "rice"), "lunch", LocalDateTime.now())
        );
        
        when(recommendationHistoryService.getRecommendationHistoryByUser(any(User.class))).thenReturn(mockHistory);
        
        // When & Then - negative page size should trigger non-paged query
        mockMvc.perform(get("/api/me/recommendations")
                        .param("page", "0")
                        .param("size", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}