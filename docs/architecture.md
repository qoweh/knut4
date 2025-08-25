# Architecture Overview

## Components
- Backend (Spring Boot): Auth, Recommendation (LLM + MapProvider), Preferences, Sharing
- Frontend (React + Vite): User input (moods, budget, location), auth pages, results display
- LLM (GPT4All/OpenAI-compatible local server): Optional intelligent menu suggestion
- Map Provider: Naver implemented (HTTP); Kakao placeholder
- Database: MySQL (prod), H2 (tests)

## High Level Flow
User -> Frontend -> Backend REST -> (MapProvider + LLM) -> Response persisted as RecommendationHistory -> Optional share token retrieval.

## Recommendation Sequence
1. Request arrives (moods, budget, lat/lon, optional weather) -> weather normalized
2. Nearby places fetched (Naver Local Search) -> distances calculated (Haversine)
3. LLM (if enabled) builds structured menu suggestions; fallback stub if disabled/timeouts
4. Preferences filter excludes allergy/disliked ingredients (string match)
5. History saved; share token can later expose read-only copy.

## Key Packages (Backend)
- domain.user: User, repository, auth services
- domain.recommendation: Service, DTOs, history, sharing entities
- domain.preference: Preference entity + controller
- domain.llm: LlmClient abstraction + HTTP (OpenAI-like) + Stub
- domain.map: MapProvider interface + Naver implementation

## Data Entities
User(id, username, passwordHash, birthDate, createdAt)
Preference(id, user FK, allergies, dislikes)
RecommendationHistory(id, user FK nullable, requestJson, responseJson, createdAt)
SharedRecommendation(id, history FK, token, createdAt)

## Extension Points
- Add KakaoMapProvider implementing MapProvider
- Introduce advanced filtering (semantic, nutrition)
- Swap GPT4All with other local/remote models via LlmClient

## Error Handling
Global exception handler maps domain exceptions to 4xx/5xx; share token / history NotFound -> 404.

## Security
JWT bearer auth for private endpoints (/api/private/**) with stateless Spring Security config.

