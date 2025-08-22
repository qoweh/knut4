package com.knut4.backend.domain.place;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kakao local search provider implementation.
 * API docs: https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword
 */
@Component
@ConditionalOnProperty(name = "app.map.provider", havingValue = "kakao")
@RequiredArgsConstructor
public class KakaoMapProvider implements MapProvider {
    private static final Logger log = LoggerFactory.getLogger(KakaoMapProvider.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${kakao.map.base-url:https://dapi.kakao.com/v2/local/search/keyword.json}")
    private String baseUrl;
    @Value("${kakao.map.rest-key:}")
    private String restKey;

    @Override
    public List<PlaceResult> search(String keyword, double latitude, double longitude, int radiusMeters) {
        if (restKey == null || restKey.isBlank()) {
            log.warn("Kakao REST API key not configured; returning empty list");
            return List.of();
        }
        try {
            WebClient client = webClientBuilder
                    .defaultHeader("Authorization", "KakaoAK " + restKey)
                    .build();
            int size = 5;
            var mono = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(baseUrl.replace("https://dapi.kakao.com", ""))
                            .queryParam("query", keyword)
                            .queryParam("y", latitude)
                            .queryParam("x", longitude)
                            .queryParam("radius", Math.min(radiusMeters, 20000)) // kakao max 20000
                            .queryParam("page", 1)
                            .queryParam("size", size)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Response.class);
            Response r = mono.block();
            if (r == null || r.documents == null) return List.of();
            return r.documents.stream()
                    .map(d -> toPlaceResult(d, latitude, longitude))
                    .sorted(Comparator.comparingDouble(PlaceResult::distanceMeters))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Kakao map search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private PlaceResult toPlaceResult(Document d, double baseLat, double baseLon) {
        try {
            double lat = Double.parseDouble(d.y);
            double lon = Double.parseDouble(d.x);
            double distance = NaverMapProvider.haversineMeters(baseLat, baseLon, lat, lon); // reuse util
            String address = d.road_address_name != null && !d.road_address_name.isBlank() ? d.road_address_name : d.address_name;
            return new PlaceResult(d.place_name, lat, lon, address, distance);
        } catch (Exception ex) {
            log.debug("Skip k document parse error: {}", ex.getMessage());
            return null;
        }
    }

    static class Response { List<Document> documents; }
    static class Document { String place_name; String x; String y; String address_name; String road_address_name; }
}
