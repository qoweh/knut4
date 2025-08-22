package com.knut4.backend1.recommendation.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationUtilsTest {
    
    @Test
    void calculateDistanceMeters_SameLocation() {
        // Given
        double lat = 37.5665;
        double lon = 126.9780;
        
        // When
        int distance = LocationUtils.calculateDistanceMeters(lat, lon, lat, lon);
        
        // Then
        assertThat(distance).isEqualTo(0);
    }
    
    @Test
    void calculateDistanceMeters_KnownDistance() {
        // Given - Seoul City Hall to Gangnam Station (approximately 8.8km)
        double seoulCityHallLat = 37.5665;
        double seoulCityHallLon = 126.9780;
        double gangnamStationLat = 37.4979;
        double gangnamStationLon = 127.0276;
        
        // When
        int distance = LocationUtils.calculateDistanceMeters(
            seoulCityHallLat, seoulCityHallLon, 
            gangnamStationLat, gangnamStationLon);
        
        // Then
        assertThat(distance).isBetween(8000, 10000); // Approximately 8.8km
    }
    
    @Test
    void calculateDistanceKm_SameLocation() {
        // Given
        double lat = 37.5665;
        double lon = 126.9780;
        
        // When
        double distance = LocationUtils.calculateDistanceKm(lat, lon, lat, lon);
        
        // Then
        assertThat(distance).isEqualTo(0.0);
    }
    
    @Test
    void calculateWalkingTimeMinutes_ShortDistance() {
        // Given
        int distanceMeters = 400; // 400 meters
        
        // When
        int walkingTime = LocationUtils.calculateWalkingTimeMinutes(distanceMeters);
        
        // Then
        assertThat(walkingTime).isEqualTo(6); // 400m at 4km/h = 6 minutes
    }
    
    @Test
    void calculateWalkingTimeMinutes_LongDistance() {
        // Given
        int distanceMeters = 2000; // 2km
        
        // When
        int walkingTime = LocationUtils.calculateWalkingTimeMinutes(distanceMeters);
        
        // Then
        assertThat(walkingTime).isEqualTo(30); // 2km at 4km/h = 30 minutes
    }
    
    @Test
    void calculateDistanceAndTime() {
        // Given
        double lat1 = 37.5665;
        double lon1 = 126.9780;
        double lat2 = 37.5675;
        double lon2 = 126.9790;
        
        // When
        int[] result = LocationUtils.calculateDistanceAndTime(lat1, lon1, lat2, lon2);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result[0]).isGreaterThan(0); // distance in meters
        assertThat(result[1]).isGreaterThan(0); // time in minutes
        
        // Verify consistency with individual methods
        int expectedDistance = LocationUtils.calculateDistanceMeters(lat1, lon1, lat2, lon2);
        int expectedTime = LocationUtils.calculateWalkingTimeMinutes(expectedDistance);
        
        assertThat(result[0]).isEqualTo(expectedDistance);
        assertThat(result[1]).isEqualTo(expectedTime);
    }
}