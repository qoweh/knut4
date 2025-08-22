# 빠른 시작 가이드

## 개발 환경 설정

### 1. 필수 사전 요구사항
- Java 17 이상
- Docker & Docker Compose
- Git

### 2. 프로젝트 클론 및 설정

```bash
# 저장소 클론
git clone https://github.com/qoweh/knut4.git
cd knut4

# 환경설정 파일 생성
cp .env.example .env
cp backend1/src/main/resources/application-secret.example.yml backend1/src/main/resources/application-secret.yml

# 필요시 환경변수 값 수정
# vi .env
# vi backend1/src/main/resources/application-secret.yml
```

### 3. Docker로 개발 환경 시작

```bash
# 데이터베이스만 실행 (백엔드는 IDE에서 실행할 경우)
cd docker
docker compose up db -d

# 또는 전체 서비스 실행
docker compose up -d

# 로그 확인
docker compose logs -f

# 서비스 중지
docker compose down
```

### 4. 백엔드 개발 실행

```bash
# Gradle을 이용한 빌드 및 실행
cd backend1
./gradlew bootRun

# 또는 IDE에서 Backend1Application.java 실행
```

### 5. 접속 확인

- **백엔드 API**: http://localhost:8080/api/v1
- **Health Check**: http://localhost:8080/api/v1/actuator/health
- **데이터베이스**: localhost:5432 (knut4/knut4_user/knut4_password)

### 6. 개발 워크플로우

```bash
# 새 기능 개발 시
git checkout develop
git pull origin develop
git checkout -b feature/123-new-feature

# 개발 후
git add .
git commit -m "feat(auth): add user registration endpoint"
git push origin feature/123-new-feature

# GitHub에서 PR 생성
```

## 유용한 명령어

### 백엔드 관련
```bash
# 테스트 실행
./gradlew test

# 빌드 (테스트 제외)
./gradlew build -x test

# 종속성 확인
./gradlew dependencies

# 코드 스타일 확인
./gradlew checkstyleMain
```

### Docker 관련
```bash
# 컨테이너 상태 확인
docker compose ps

# 로그 실시간 확인
docker compose logs -f backend

# 데이터베이스 접속
docker compose exec db psql -U knut4_user -d knut4

# 볼륨 삭제 (데이터 초기화)
docker compose down -v
```

### 데이터베이스 관련
```bash
# PostgreSQL 컨테이너에 직접 접속
docker compose exec db bash

# SQL 파일 실행
docker compose exec -T db psql -U knut4_user -d knut4 < script.sql
```

## 트러블슈팅

### 포트 충돌
```bash
# 사용 중인 포트 확인
lsof -i :8080
lsof -i :5432

# Docker 컨테이너 강제 종료
docker compose down --remove-orphans
```

### 데이터베이스 연결 오류
```bash
# 컨테이너 재시작
docker compose restart db

# 로그 확인
docker compose logs db
```

### 빌드 오류
```bash
# Gradle 캐시 정리
./gradlew clean

# Gradle Wrapper 재다운로드
./gradlew wrapper --gradle-version 8.14.3
```

### 권한 오류 (Linux/Mac)
```bash
# gradlew 실행 권한 부여
chmod +x gradlew

# Docker 그룹에 사용자 추가
sudo usermod -aG docker $USER
```

## 개발 팁

1. **IDE 설정**: IntelliJ IDEA 또는 VS Code 사용 권장
2. **Hot Reload**: Spring Boot DevTools 활성화시 자동 재시작
3. **API 테스트**: Postman 또는 curl 사용
4. **로그 레벨**: 개발시 DEBUG, 운영시 INFO 사용
5. **브랜치 전략**: 반드시 이슈 기반으로 브랜치 생성

## 다음 단계

1. 사용자 인증 API 구현
2. 프론트엔드 React 앱 생성
3. 실제 비즈니스 로직 개발
4. 테스트 케이스 작성
5. CI/CD 파이프라인 완성