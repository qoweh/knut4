package com.knut4.backend.domain.llm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LlmContextTest {
    
    @Test
    void contextLoadsWithLlmDisabled() {
        // This test ensures that the application context loads correctly
        // when LLM is disabled (default test configuration)
    }
}