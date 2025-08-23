package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.User;
import com.knut4.backend.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationHistoryDedupTest {
    @Autowired RecommendationService recommendationService;
    @Autowired RecommendationHistoryRepository historyRepository;
    @Autowired UserRepository userRepository;
    @Autowired com.knut4.backend.domain.preference.repository.PreferenceRepository preferenceRepository;

    @BeforeEach
    void setup() {
        historyRepository.deleteAll();
    // delete preferences first to satisfy FK constraint user_preference.user_id -> users.id
    preferenceRepository.deleteAll();
        userRepository.deleteAll();
        User u = new User();
        u.setUsername("tester");
        u.setPasswordHash("pw");
        userRepository.save(u);
    }

    @Test
    @WithMockUser(username = "tester")
    void duplicateImmediateRecommendationsAreDeduped() {
        RecommendationRequest req = new RecommendationRequest("cloudy", java.util.List.of("든든"), 10000, 37.0, 127.0);
    recommendationService.recommend(req);
    recommendationService.recommend(req); // second identical call quickly
        var user = userRepository.findByUsername("tester").orElseThrow();
        long count = historyRepository.countByUser(user);
        assertThat(count).isEqualTo(1L);
    }
}
