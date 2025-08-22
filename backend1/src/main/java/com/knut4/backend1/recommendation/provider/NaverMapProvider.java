package com.knut4.backend1.recommendation.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Collections;

/**
 * Naver Map API implementation for place search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NaverMapProvider implements MapProvider {
    
    private final WebClient webClient;
    
    @Value("${naver.map.client-id}")
    private String clientId;
    
    @Value("${naver.map.client-secret}")
    private String clientSecret;
    
    private static final String NAVER_SEARCH_API = "https://openapi.naver.com/v1/search/local.json";
    
    @Override
    public List<PlaceSearchResult> searchPlaces(String keyword, double lat, double lon, Integer radius) {
        log.info("Searching places with keyword: {}, lat: {}, lon: {}, radius: {}", keyword, lat, lon, radius);
        
        try {
            // Build query with location bias
            String query = keyword;
            
            // Call Naver Local Search API
            NaverSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", "5") // limit to 5 results
                    .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(NaverSearchResponse.class)
                .block();
                
            if (response != null && response.items() != null) {
                return response.items().stream()
                    .map(item -> new PlaceSearchResult(
                        removeHtmlTags(item.title()),
                        item.category(),
                        parseLatLon(item.mapy()),
                        parseLatLon(item.mapx()),
                        removeHtmlTags(item.address())
                    ))
                    .toList();
            }
            
        } catch (Exception e) {
            log.error("Error searching places with Naver API", e);
        }
        
        // Return empty list on error or no results
        return Collections.emptyList();
    }
    
    private double parseLatLon(String coord) {
        try {
            // Naver API returns coordinates as string in specific format
            return Double.parseDouble(coord) / 10000000.0;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse coordinate: {}", coord);
            return 0.0;
        }
    }
    
    private String removeHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "");
    }
    
    /**
     * Naver Local Search API Response DTO
     */
    public record NaverSearchResponse(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<NaverSearchItem> items
    ) {}
    
    public record NaverSearchItem(
        String title,
        String link,
        String category,
        String description,
        String telephone,
        String address,
        String roadAddress,
        String mapx,
        String mapy
    ) {}
}