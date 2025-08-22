## 오늘 뭐 먹지? (Local Edition)

Monorepo containing:

- backend: Spring Boot 3 (Java 17) REST API with JWT auth, menu recommendation, pluggable map providers (Naver implemented, Kakao placeholder), OpenAPI docs.
- frontend: Vite + React + TypeScript scaffold (to be expanded) for user interaction (conditions input, results view, auth pages).
- infrastructure: Planned Docker / docker-compose for FE, BE, MySQL, GPT4All local LLM.

### Tech Stack
Backend: Spring Boot, Spring Security (JWT), JPA (MySQL/H2 test), WebFlux (WebClient), springdoc-openapi
Frontend: React, TypeScript, Vite (scaffold WIP)
AI: GPT4All (integration TODO)
DB: MySQL (H2 for tests)
Build: Gradle (Java toolchain 17)
Test: JUnit5, Mockito, Spring Boot Test
Container: Docker & docker-compose (upcoming)

### Features (Progress)
- [x] User signup / login (JWT access token)
- [x] Recommendation endpoint skeleton using Naver Local Search
- [x] Strategy pattern for MapProvider (Naver implemented, Kakao placeholder)
- [x] Recommendation history entity + persistence
- [x] Unit tests (auth, recommendation service, map provider utils)
- [x] Integration tests (auth + recommendation)
- [ ] Frontend pages (Main, Auth, Results, My Page)
- [ ] GPT4All integration to generate menu candidates & reasons
- [ ] Kakao map provider implementation & provider selection
- [ ] Docker / compose setup (FE/BE/DB/LLM)
- [ ] My Page history & preferences APIs
- [ ] Shareable recommendation links

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

### License
Proprietary (adjust later).
