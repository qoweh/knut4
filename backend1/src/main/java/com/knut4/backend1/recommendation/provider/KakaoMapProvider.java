package com.knut4.backend1.recommendation.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Kakao Map API implementation placeholder
 * TODO: Implement Kakao Map API integration when needed
 */
@Service
@ConditionalOnProperty(name = "kakao.map.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class KakaoMapProvider implements MapProvider {
    
    @Override
    public List<PlaceSearchResult> searchPlaces(String keyword, double lat, double lon, Integer radius) {
        log.info("KakaoMapProvider not implemented yet. Keyword: {}, lat: {}, lon: {}, radius: {}", 
                 keyword, lat, lon, radius);
        
        // TODO: Implement Kakao Local API integration
        // - Configure Kakao API key
        // - Call Kakao Local Search API
        // - Parse response and convert to PlaceSearchResult
        // - Handle errors appropriately
        
        return Collections.emptyList();
    }
}