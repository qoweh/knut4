package com.knut4.backend1.recommendation.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for distance and time calculations
 */
@UtilityClass
public class LocationUtils {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_WALKING_SPEED_KMH = 4.0; // 4 km/h average walking speed
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * 
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in meters
     */
    public static int calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = calculateDistanceKm(lat1, lon1, lat2, lon2);
        return (int) Math.round(distanceKm * 1000);
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * 
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Calculate estimated walking time based on distance
     * 
     * @param distanceMeters distance in meters
     * @return estimated walking time in minutes
     */
    public static int calculateWalkingTimeMinutes(int distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;
        double timeHours = distanceKm / AVERAGE_WALKING_SPEED_KMH;
        return (int) Math.round(timeHours * 60);
    }
    
    /**
     * Calculate both distance and walking time between two coordinates
     * 
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return array with [distanceMeters, durationMinutes]
     */
    public static int[] calculateDistanceAndTime(double lat1, double lon1, double lat2, double lon2) {
        int distanceMeters = calculateDistanceMeters(lat1, lon1, lat2, lon2);
        int durationMinutes = calculateWalkingTimeMinutes(distanceMeters);
        return new int[]{distanceMeters, durationMinutes};
    }
}