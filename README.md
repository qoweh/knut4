## 오늘 뭐 먹지? (Local Edition)

Monorepo containing:

- backend: Spring Boot 3 (Java 17) REST API with JWT auth, menu recommendation, pluggable map providers (Naver implemented, Kakao placeholder), OpenAPI docs.
- frontend: Vite + React + TypeScript scaffold (to be expanded) for user interaction (conditions input, results view, auth pages).
- infrastructure: Docker / docker-compose for FE, BE, MySQL, GPT4All local LLM (optional local model).

### Tech Stack
Backend: Spring Boot, Spring Security (JWT), JPA (MySQL/H2 test), WebFlux (WebClient), springdoc-openapi
Frontend: React, TypeScript, Vite (scaffold evolving)
AI: GPT4All (optional local integration via OpenAI-compatible endpoint)
DB: MySQL (H2 for tests)
Build: Gradle (Java toolchain 17)
Test: JUnit5, Mockito, Spring Boot Test
Container: Docker & docker-compose

### Features (Progress)
- [x] User signup / login (JWT access token)
- [x] Recommendation endpoint skeleton using Naver Local Search
- [x] Strategy pattern for MapProvider (Naver implemented, Kakao placeholder)
- [x] Recommendation history entity + persistence
- [x] Unit tests (auth, recommendation service, map provider utils)
- [x] Integration tests (auth + recommendation)
- [~] Frontend pages (Main + Results + Preferences partial) (Auth/My Page history pending)
- [x] GPT4All integration abstraction + HTTP client (server container wiring experimental)
- [ ] Kakao map provider implementation & provider selection (scoped out: Naver only for now)
- [x] Docker / compose setup (FE/BE/DB/LLM – GPT4All container optional)
- [x] Preferences APIs (CRUD upsert + fetch)
- [x] Shareable recommendation links

### Running Backend (Dev)
Create `backend/src/main/resources/application-secret.yml` (ignored) with:
```
app:
  jwt:
    secret: <BASE64_>=
    issuer: knut4
    access-token-validity-seconds: 3600
    refresh-token-validity-seconds: 1209600
naver:
  map:
    client-id: <your-id>
    client-secret: <your-secret>
```
Run: `./gradlew bootRun`

### Tests
`./gradlew test` – includes unit + integration tests. Recommendation integration works without Naver credentials (returns empty place list gracefully).

### Docker Compose
```
cp .env.example .env  # then optionally adjust vars
docker compose up --build
```
Services:
- backend: http://localhost:8080
- frontend: http://localhost:3000
- mysql: localhost:3306 (app/app)
- gpt4all: http://localhost:4891 (if image provides API)

LLM env vars configured in `docker-compose.yml` (backend section). LLM is optional; backend falls back to heuristic suggestions if unreachable.

Model cache: gpt4all container mounts `gpt4all_models` volume at `/models` (place downloaded models there if image does not auto-fetch).

Environment: `.env.example` provided (copy to `.env`). Secret JWT & API keys live in `backend/src/main/resources/application-secret.yml` (copy from the provided example file in backend root).

CI: GitHub Actions workflow (`ci.yml`) builds & tests backend (stub LLM) and builds frontend. ![CI](https://github.com/qoweh/knut4/actions/workflows/ci.yml/badge.svg)

### Documentation
See docs:
- docs/architecture.md
- docs/requirements.md
- docs/test-cases.md

### License
Proprietary (adjust later).
