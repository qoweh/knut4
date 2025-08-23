package com.knut4.backend.domain.llm;

import java.util.List;
// StructuredMenuPlace record in same package

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

    /**
     * Advanced structured recommendation: given raw place samples (serialized JSON array objects with name, distanceMeters, category)
     * ask model to produce up to menuMax menus, each mapped to 1-2 place names from the list and a short reason.
     * Implementations may fallback to simple suggestMenus when unsupported.
     * Expected response (model instruction): lines like: 메뉴명 | place1,place2 | 이유
     */
    default List<StructuredMenuPlace> suggestMenusWithPlaces(List<String> moods,
                                                             String weather,
                                                             Integer budget,
                                                             Double latitude,
                                                             Double longitude,
                                                             String placeSamplesJson,
                                                             int menuMax) {
        // Fallback: call basic version and leave places empty
        return suggestMenus(moods, weather, budget, latitude, longitude, List.of(), menuMax).stream()
                .map(s -> new StructuredMenuPlace(s.menu(), List.of(), s.reason()))
                .toList();
    }

    /** Legacy simple method kept for backward compatibility (will adapt to new). */
    @Deprecated
    default List<String> legacySuggestMenus(List<String> moods, String weather, int max) {
        return suggestMenus(moods, weather, null, null, null, List.of(), max).stream().map(LlmMenuSuggestion::menu).toList();
    }
}

/** Simple DTO for LLM menu suggestions (StructuredMenuPlace in separate file). */
