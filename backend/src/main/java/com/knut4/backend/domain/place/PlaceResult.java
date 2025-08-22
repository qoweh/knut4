package com.knut4.backend.domain.place;

/** Simple projection of a place returned by map providers. */
public record PlaceResult(
        String name,
        double latitude,
        double longitude,
        String address,
        double distanceMeters
) {}
