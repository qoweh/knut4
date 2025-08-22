package com.knut4.backend.domain.llm;

import java.util.List;

/**
 * Abstraction for local LLM (e.g., GPT4All) suggestions.
 */
public interface LlmClient {
    /**
     * Generate up to max menu suggestions based on moods and weather context.
     */
    List<String> suggestMenus(List<String> moods, String weather, int max);
}
