package com.knut4.backend.domain.place;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverMapProvider implements MapProvider {

    private static final Logger log = LoggerFactory.getLogger(NaverMapProvider.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${naver.map.base-url:https://openapi.naver.com/v1/search/local.json}")
    private String baseUrl;
    @Value("${naver.map.client-id:}")
    private String clientId;
    @Value("${naver.map.client-secret:}")
    private String clientSecret;

    @Override
    public List<PlaceResult> search(String keyword, double latitude, double longitude, int radiusMeters) {
        // TODO real API integration: Naver local search doesn't support lat/lon directly; needs query + display/sort.
        if (clientId == null || clientId.isBlank()) {
            log.warn("Naver clientId not configured; returning empty result");
            return List.of();
        }
        try {
            // Placeholder call (not executed yet) - we'll implement parsing next iteration.
            WebClient client = webClientBuilder.build();
            // Keeping skeleton; returning empty list until response mapping added.
            return List.of();
        } catch (Exception e) {
            log.error("Naver map search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
