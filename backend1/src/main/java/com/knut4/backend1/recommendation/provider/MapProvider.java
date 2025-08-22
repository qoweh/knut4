package com.knut4.backend1.recommendation.provider;

import java.util.List;

/**
 * Strategy interface for map providers to search for places
 */
public interface MapProvider {
    
    /**
     * Search for places based on keyword and location
     * 
     * @param keyword search keyword (e.g., "restaurant", "cafe")
     * @param lat latitude
     * @param lon longitude
     * @param radius search radius in meters (optional)
     * @return list of found places
     */
    List<PlaceSearchResult> searchPlaces(String keyword, double lat, double lon, Integer radius);
    
    /**
     * Result of place search
     */
    record PlaceSearchResult(
        String name,
        String category,
        Double lat,
        Double lon,
        String address
    ) {}
}