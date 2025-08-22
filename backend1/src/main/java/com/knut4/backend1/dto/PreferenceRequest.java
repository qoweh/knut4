package com.knut4.backend1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceRequest {
    
    @NotNull(message = "Allergies list cannot be null")
    private List<String> allergies;
    
    @NotNull(message = "Dislikes list cannot be null")
    private List<String> dislikes;
}