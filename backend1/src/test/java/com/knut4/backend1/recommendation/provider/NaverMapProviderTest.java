package com.knut4.backend1.recommendation.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaverMapProviderTest {
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    private NaverMapProvider naverMapProvider;
    
    @BeforeEach
    void setUp() {
        naverMapProvider = new NaverMapProvider(webClient);
        ReflectionTestUtils.setField(naverMapProvider, "clientId", "test-client-id");
        ReflectionTestUtils.setField(naverMapProvider, "clientSecret", "test-client-secret");
    }
    
    @Test
    void searchPlaces_Success() {
        // Given
        NaverMapProvider.NaverSearchResponse mockResponse = new NaverMapProvider.NaverSearchResponse(
            "Mon, 01 Jan 2024 00:00:00 +0900",
            1,
            1,
            1,
            List.of(new NaverMapProvider.NaverSearchItem(
                "테스트 맛집",
                "http://example.com",
                "한식>일반한식",
                "맛있는 한식 전문점",
                "02-1234-5678",
                "서울시 강남구 테스트로 123",
                "서울시 강남구 테스트대로 456",
                "1270000000", // longitude * 10^7
                "375000000"   // latitude * 10^7
            ))
        );
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverMapProvider.NaverSearchResponse.class))
            .thenReturn(Mono.just(mockResponse));
        
        // When
        List<MapProvider.PlaceSearchResult> results = naverMapProvider.searchPlaces(
            "맛집", 37.5, 127.0, 1000);
        
        // Then
        assertThat(results).hasSize(1);
        MapProvider.PlaceSearchResult result = results.get(0);
        assertThat(result.name()).isEqualTo("테스트 맛집");
        assertThat(result.category()).isEqualTo("한식>일반한식");
        assertThat(result.lat()).isEqualTo(37.5);
        assertThat(result.lon()).isEqualTo(127.0);
        assertThat(result.address()).isEqualTo("서울시 강남구 테스트로 123");
    }
    
    @Test
    void searchPlaces_EmptyResponse() {
        // Given
        NaverMapProvider.NaverSearchResponse mockResponse = new NaverMapProvider.NaverSearchResponse(
            "Mon, 01 Jan 2024 00:00:00 +0900",
            0,
            1,
            0,
            List.of()
        );
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverMapProvider.NaverSearchResponse.class))
            .thenReturn(Mono.just(mockResponse));
        
        // When
        List<MapProvider.PlaceSearchResult> results = naverMapProvider.searchPlaces(
            "존재하지않는장소", 37.5, 127.0, 1000);
        
        // Then
        assertThat(results).isEmpty();
    }
    
    @Test
    void searchPlaces_ApiError() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverMapProvider.NaverSearchResponse.class))
            .thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // When
        List<MapProvider.PlaceSearchResult> results = naverMapProvider.searchPlaces(
            "맛집", 37.5, 127.0, 1000);
        
        // Then
        assertThat(results).isEmpty();
    }
    
    @Test
    void searchPlaces_HtmlTagsRemoved() {
        // Given
        NaverMapProvider.NaverSearchResponse mockResponse = new NaverMapProvider.NaverSearchResponse(
            "Mon, 01 Jan 2024 00:00:00 +0900",
            1,
            1,
            1,
            List.of(new NaverMapProvider.NaverSearchItem(
                "<b>테스트</b> 맛집",
                "http://example.com",
                "한식>일반한식",
                "맛있는 한식 전문점",
                "02-1234-5678",
                "<b>서울시</b> 강남구 테스트로 123",
                "서울시 강남구 테스트대로 456",
                "1270000000",
                "375000000"
            ))
        );
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverMapProvider.NaverSearchResponse.class))
            .thenReturn(Mono.just(mockResponse));
        
        // When
        List<MapProvider.PlaceSearchResult> results = naverMapProvider.searchPlaces(
            "맛집", 37.5, 127.0, 1000);
        
        // Then
        assertThat(results).hasSize(1);
        MapProvider.PlaceSearchResult result = results.get(0);
        assertThat(result.name()).isEqualTo("테스트 맛집");
        assertThat(result.address()).isEqualTo("서울시 강남구 테스트로 123");
    }
}