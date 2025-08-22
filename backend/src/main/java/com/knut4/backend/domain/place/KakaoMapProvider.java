package com.knut4.backend.domain.place;

/**
 * Placeholder Kakao provider for future implementation.
 * Not annotated with @Component yet to avoid multiple MapProvider beans.
 */
public class KakaoMapProvider implements MapProvider {
    @Override
    public java.util.List<PlaceResult> search(String keyword, double latitude, double longitude, int radiusMeters) {
        return java.util.List.of();
    }
}
