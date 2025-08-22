package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/private/history")
@RequiredArgsConstructor
public class HistoryController {
    private final RecommendationHistoryRepository repository;

    @GetMapping
    public ResponseEntity<List<RecommendationHistory>> list(Authentication auth) {
        // TODO: filter by authenticated user when user linking implemented
        return ResponseEntity.ok(repository.findAll());
    }
}
