# KNUT4 프로젝트

## 프로젝트 개요
KNUT4는 Spring Boot 백엔드와 프론트엔드를 포함한 풀스택 웹 애플리케이션입니다.

## 기술 스택

### 백엔드
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.4
- **데이터베이스**: PostgreSQL
- **빌드 툴**: Gradle 8.14.3
- **인증**: JWT (Spring Security)
- **ORM**: Spring Data JPA

### 프론트엔드
- **언어**: TypeScript
- **프레임워크**: React (예정)
- **빌드 툴**: Vite (예정)

### 인프라
- **컨테이너**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **배포**: (예정)

## 브랜치 전략

Git-flow 기반 브랜치 전략을 따릅니다:

- `main`: 운영 배포 브랜치 (최신 안정 버전)
- `develop`: 개발 통합 브랜치 (모든 기능 브랜치가 머지되는 기본 브랜치)
- `feature/<issue-number>-<desc>`: 신규 기능 개발 (예: `feature/23-user-signup`)
- `fix/<issue-number>-<desc>`: 버그 수정 (예: `fix/45-login-error`)
- `hotfix/<issue-number>-<desc>`: 긴급 수정

자세한 내용은 [backend1/COMMIT_RULES.md](backend1/COMMIT_RULES.md)를 참고하세요.

## 커밋 컨벤션

Conventional Commits 규칙을 따릅니다:

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **type**: feat, fix, docs, style, refactor, test, chore
- **scope**: 변경한 모듈/패키지 (선택)
- **subject**: 한 줄 요약 (명령형, 50자 이내)
- **body**: 상세 설명 (선택)
- **footer**: 이슈 번호나 BREAKING CHANGE

예시:
```
feat(auth): add JWT token refresh endpoint

Add refresh-token endpoint to renew access tokens without re-login.

Closes #12
```

## 시크릿 관리 규칙

1. **절대 금지**: 프로덕션 시크릿을 코드에 하드코딩하거나 커밋하지 마세요
2. **환경변수 사용**: 모든 민감한 정보는 환경변수로 관리
3. **예시 파일 제공**: `.env.example`, `application-secret.example.yml` 파일로 필요한 설정 가이드 제공
4. **Git 제외**: `.gitignore`에 실제 시크릿 파일들 등록
5. **로컬 개발**: 개발 환경용 더미 값 사용, 프로덕션과 구분

## CI/CD

GitHub Actions를 통한 자동화된 빌드, 테스트, 배포 파이프라인을 구성했습니다. [.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) 참고.

## 프로젝트 구조

```
├── backend1/           # Spring Boot 백엔드
├── frontend/           # React 프론트엔드 (예정)
├── docker/             # Docker 설정 파일들
├── docs/               # 프로젝트 문서
├── .github/workflows/  # GitHub Actions 워크플로우
└── README.md          # 이 파일
```

## 시작하기

### 개발 환경 설정

1. **필수 요구사항**
   - Java 17+
   - Docker & Docker Compose
   - Git

2. **저장소 클론**
   ```bash
   git clone https://github.com/qoweh/knut4.git
   cd knut4
   ```

3. **환경설정 파일 복사**
   ```bash
   cp .env.example .env
   cp backend1/src/main/resources/application-secret.example.yml backend1/src/main/resources/application-secret.yml
   ```

4. **Docker로 개발 환경 실행**
   ```bash
   docker-compose up -d
   ```

5. **백엔드 빌드 및 실행**
   ```bash
   cd backend1
   ./gradlew bootRun
   ```

## 문서

- [아키텍처 문서](docs/architecture.md)
- [테스트 케이스](docs/test-cases.md)
- [요구사항 문서](docs/requirements.md)
- [백엔드 개발 가이드](backend1/AGENT_README.md)

## 기여하기

1. 이슈 생성 후 브랜치 생성
2. 커밋 컨벤션 준수
3. PR 생성 및 리뷰 요청
4. 테스트 통과 확인 후 머지

자세한 내용은 [backend1/COMMIT_RULES.md](backend1/COMMIT_RULES.md)를 참고하세요.

## 라이센스

[라이센스 정보 추가 예정]