package com.knut4.backend.domain.place;

import java.util.List;

/**
 * Strategy interface for map place search (currently Naver implementation only; Kakao deferred/out of scope).
 */
public interface MapProvider {
    /**
     * Search places near given coordinate.
     * @param keyword search keyword
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters optional radius (meters) - provider may approximate
     * @return list of place results (can be empty)
     */
    List<PlaceResult> search(String keyword, double latitude, double longitude, int radiusMeters);
}
