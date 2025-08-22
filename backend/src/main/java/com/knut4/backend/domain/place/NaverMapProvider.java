package com.knut4.backend.domain.place;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    public List<PlaceResult> search(String keyword, double latitude, double longitude, int radiusMeters) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.warn("Naver API credentials not configured; returning empty list");
            return List.of();
        }
        try {
            WebClient client = webClientBuilder
                    .defaultHeader("X-Naver-Client-Id", clientId)
                    .defaultHeader("X-Naver-Client-Secret", clientSecret)
                    .build();

            int display = 5; // fetch up to 5 items for initial recommendation context
            Mono<LocalSearchResponse> mono = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(baseUrl.replace("https://openapi.naver.com", ""))
                            .queryParam("query", keyword)
                            .queryParam("display", display)
                            .queryParam("start", 1)
                            .queryParam("sort", "random")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(LocalSearchResponse.class);

            LocalSearchResponse response = mono.block();
            if (response == null || response.items == null) return List.of();
            return response.items.stream()
                    .map(item -> toPlaceResult(item, latitude, longitude))
                    .filter(p -> p != null)
                    .sorted(Comparator.comparingDouble(PlaceResult::distanceMeters))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Naver map search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private PlaceResult toPlaceResult(Item item, double baseLat, double baseLon) {
        try {
            double lon = Double.parseDouble(item.mapx); // mapx(mapy) now WGS84 per 2023 change
            double lat = Double.parseDouble(item.mapy);
            double distance = haversineMeters(baseLat, baseLon, lat, lon);
            String title = sanitize(item.title);
            String address = item.roadAddress != null && !item.roadAddress.isBlank() ? item.roadAddress : item.address;
            return new PlaceResult(title, lat, lon, address, distance);
        } catch (Exception ex) {
            log.debug("Skip item due to parse error: {}", ex.getMessage());
            return null;
        }
    }

    static String sanitize(String s) {
        if (s == null) return null;
        return TAG_PATTERN.matcher(s).replaceAll("");
    }

    static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371_000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Minimal subset of fields we use from response
    static class LocalSearchResponse { List<Item> items; }
    static class Item { String title; String address; String roadAddress; String mapx; String mapy; }
}
