package com.knut4.backend.domain.recommendation.controller;

import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.UserRepository;
import com.knut4.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<HistoryPageResponse> list(Authentication auth, Integer page, Integer size) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 && size <= 100 ? size : 10;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (auth == null || !(auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User springUser)) {
            return ResponseEntity.ok(HistoryPageResponse.empty());
        }
        String username = springUser.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(HistoryPageResponse.empty());
        }
        Page<RecommendationHistory> pageResult = repository.findByUserOrderByCreatedAtDesc(user, pageable);
        return ResponseEntity.ok(HistoryPageResponse.from(pageResult));
    }

    public record HistoryItem(Long id, String weather, String moods, Integer budget, Double latitude, Double longitude, java.time.Instant createdAt) {
        static HistoryItem from(RecommendationHistory h) {
            return new HistoryItem(h.getId(), h.getWeather(), h.getMoods(), h.getBudget(), h.getLatitude(), h.getLongitude(), h.getCreatedAt());
        }
    }

    public record HistoryPageResponse(List<HistoryItem> content, int page, int size, long totalElements, int totalPages) {
        static HistoryPageResponse from(Page<RecommendationHistory> page) {
            List<HistoryItem> items = page.getContent().stream().map(HistoryItem::from).toList();
            return new HistoryPageResponse(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
        static HistoryPageResponse empty() { return new HistoryPageResponse(List.of(), 0, 0, 0, 0); }
    }
}
