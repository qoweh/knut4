package com.knut4.backend.domain.recommendation;

import com.knut4.backend.domain.place.MapProvider;
import com.knut4.backend.domain.place.PlaceResult;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import com.knut4.backend.domain.recommendation.dto.ShareRecommendationResponse;
import com.knut4.backend.domain.recommendation.dto.SharedRecommendationResponse;
import com.knut4.backend.domain.recommendation.entity.RecommendationHistory;
import com.knut4.backend.domain.recommendation.repository.RecommendationHistoryRepository;
import com.knut4.backend.domain.user.User;
import com.knut4.backend.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RecommendationSharingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationHistoryRepository historyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MapProvider mapProvider;

    private User testUser;
    private User otherUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Mock MapProvider to return sample results
        when(mapProvider.search(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new PlaceResult("Test Restaurant", 37.5665, 126.9780, "Test Address", 100.0)));

        // Create test users
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        userRepository.save(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPasswordHash(passwordEncoder.encode("password"));
        otherUser.setBirthDate(LocalDate.of(1990, 1, 1));
        userRepository.save(otherUser);

        // Login and get token
        String loginRequest = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @DisplayName("추천 -> 공유 -> 공개 조회 통합 테스트")
    void recommendSharePublicFetchFlow() throws Exception {
        // 1. Make recommendation
        String recommendRequest = """
                {
                    "weather": "sunny",
                    "moods": ["매콤"],
                    "budget": 15000,
                    "latitude": 37.5665,
                    "longitude": 126.9780
                }
                """;

        mockMvc.perform(post("/api/private/recommendations")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recommendRequest))
                .andExpect(status().isOk());

        // 2. Share recommendation (without historyId - should use latest)
        MvcResult shareResult = mockMvc.perform(post("/api/private/recommendations/share")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String shareResponse = shareResult.getResponse().getContentAsString();
        ShareRecommendationResponse shareDto = objectMapper.readValue(shareResponse, ShareRecommendationResponse.class);
        String token = shareDto.token();
        assertThat(token).isNotBlank();

        // 3. Access shared recommendation publicly
        MvcResult publicResult = mockMvc.perform(get("/api/public/recommendations/shared/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weather").value("sunny"))
                .andExpect(jsonPath("$.moods").value("매콤"))
                .andExpect(jsonPath("$.budget").value(15000))
                .andExpect(jsonPath("$.latitude").value(37.5665))
                .andExpect(jsonPath("$.longitude").value(126.9780))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String publicResponse = publicResult.getResponse().getContentAsString();
        SharedRecommendationResponse publicDto = objectMapper.readValue(publicResponse, SharedRecommendationResponse.class);
        assertThat(publicDto.weather()).isEqualTo("sunny");
        assertThat(publicDto.moods()).isEqualTo("매콤");
        assertThat(publicDto.message()).contains("새로 생성하려면");
    }

    @Test
    @DisplayName("같은 기록 재공유시 동일 토큰 반환")
    void shareExistingHistoryReturnsSameToken() throws Exception {
        // Create a history manually
        RecommendationHistory history = new RecommendationHistory();
        history.setUser(testUser);
        history.setWeather("rainy");
        history.setMoods("따뜻한");
        history.setBudget(20000);
        history.setLatitude(37.5665);
        history.setLongitude(126.9780);
        historyRepository.save(history);

        // Share it twice
        MvcResult firstShare = mockMvc.perform(post("/api/private/recommendations/share")
                        .param("historyId", history.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondShare = mockMvc.perform(post("/api/private/recommendations/share")
                        .param("historyId", history.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String firstToken = objectMapper.readTree(firstShare.getResponse().getContentAsString()).get("token").asText();
        String secondToken = objectMapper.readTree(secondShare.getResponse().getContentAsString()).get("token").asText();

        assertThat(firstToken).isEqualTo(secondToken);
    }

    @Test
    @DisplayName("다른 사용자 기록 공유 시도시 404")
    void shareOtherUserHistoryReturns404() throws Exception {
        // Create history for other user
        RecommendationHistory otherHistory = new RecommendationHistory();
        otherHistory.setUser(otherUser);
        otherHistory.setWeather("cloudy");
        otherHistory.setMoods("담백한");
        otherHistory.setBudget(10000);
        otherHistory.setLatitude(37.5665);
        otherHistory.setLongitude(126.9780);
        historyRepository.save(otherHistory);

        // Try to share other user's history
        mockMvc.perform(post("/api/private/recommendations/share")
                        .param("historyId", otherHistory.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 공개 조회시 404")
    void getSharedWithInvalidTokenReturns404() throws Exception {
        mockMvc.perform(get("/api/public/recommendations/shared/invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공유할 기록이 없을 때 404")
    void shareWithoutHistoryReturns404() throws Exception {
        // Create a new user with no history
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPasswordHash(passwordEncoder.encode("password"));
        newUser.setBirthDate(LocalDate.of(1990, 1, 1));
        userRepository.save(newUser);

        // Login as new user
        String loginRequest = """
                {
                    "username": "newuser",
                    "password": "password"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String newUserToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        // Try to share when no history exists
        mockMvc.perform(post("/api/private/recommendations/share")
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isNotFound());
    }
}