package com.knut4.backend.domain.llm;

import java.util.List;

/**
 * Abstraction for local LLM (e.g., GPT4All) suggestions.
 */
public interface LlmClient {
    /**
     * Generate up to max menu suggestions (menu + reason) based on user context and nearby place names.
     * @param moods user mood tags (can be empty)
     * @param weather current weather textual tag
     * @param budget user budget (won)
     * @param latitude target latitude
     * @param longitude target longitude
     * @param nearbyPlaceNames sampled list of nearby place names (can guide cuisine types)
     * @param max maximum number of menu suggestions desired
     */
    List<LlmMenuSuggestion> suggestMenus(List<String> moods,
                                         String weather,
                                         Integer budget,
                                         Double latitude,
                                         Double longitude,
                                         List<String> nearbyPlaceNames,
                                         int max);

    /** Legacy simple method kept for backward compatibility (will adapt to new). */
    @Deprecated
    default List<String> legacySuggestMenus(List<String> moods, String weather, int max) {
        return suggestMenus(moods, weather, null, null, null, List.of(), max).stream().map(LlmMenuSuggestion::menu).toList();
    }
}

/** Simple DTO for LLM menu suggestions. */
