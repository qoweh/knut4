package com.knut4.backend1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {
    
    private Long id;
    private List<String> allergies;
    private List<String> dislikes;
    private LocalDateTime createdAt;
}