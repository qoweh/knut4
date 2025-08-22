package com.knut4.backend.domain.place;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

public class NaverMapProviderTest {
    @Test
    void returnsEmptyListWhenNoCredentials() {
        NaverMapProvider provider = new NaverMapProvider(WebClient.builder());
        assertThat(provider.search("치킨", 37.0, 127.0, 500)).isEmpty();
    }
}
