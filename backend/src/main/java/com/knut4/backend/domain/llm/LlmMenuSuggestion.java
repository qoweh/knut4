package com.knut4.backend.domain.llm;

/** Public record representing a menu suggestion (menu + reason) from LLM. */
public record LlmMenuSuggestion(String menu, String reason) {}
