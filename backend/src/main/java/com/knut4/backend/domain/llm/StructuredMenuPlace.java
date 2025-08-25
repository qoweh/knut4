package com.knut4.backend.domain.llm;

import java.util.List;

/** Structured menu + selected place names + reason returned by advanced LLM mode. */
public record StructuredMenuPlace(String menu, List<String> places, String reason) {}
