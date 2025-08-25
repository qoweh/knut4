# Requirements (Structured)

## Core
- User can sign up / login and obtain JWT
- User can request menu recommendations with moods, budget, location (weather optional)
- System returns up to N menu items with reasons + nearby places
- Store each recommendation in history (anonymous supported)
- User can retry previous recommendation
- User can share a recommendation via public token link

## Preferences
- User can save allergies & dislikes lists
- Filtering removes menu suggestions containing those strings

## LLM (Optional)
- When enabled, LLM suggests menus & reasons using context (moods, normalized weather, nearby place names)
- Timeout fallback to baseline heuristic suggestions

## Map Provider
- Fetch nearby restaurants leveraging provider abstraction (Naver implemented)
- Calculate distances (meters) and approximate walking duration

## Non-Functional
- JWT-based stateless security for private endpoints
- Graceful degradation if external APIs (LLM, Map) fail
- Observable timing (Micrometer reflection) around recommendation flow
- Containerized deployment (backend + frontend + db + local LLM) via docker-compose

## Future (Backlog)
- Kakao provider implementation
- Advanced preference / nutrition model
- Rich frontend history & preferences UI
