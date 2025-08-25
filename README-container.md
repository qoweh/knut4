# knut4 서비스 컨테이너 배포 가이드

## 전체 구성
- backend: Spring Boot (8080)
- frontend: Vite+React 정적 빌드, Nginx (80)
- docker-compose로 통합 관리

## 빌드 및 실행
```sh
docker compose build
docker compose up -d
```

- http://localhost/ (프론트)
- http://localhost:8080/ (백엔드 API)

## 프론트엔드에서 API 연동
- `/api/`로 시작하는 요청은 자동으로 backend 컨테이너(8080)로 프록시됨 (nginx.conf 참고)

## 개발/운영 환경 분리
- backend: `SPRING_PROFILES_ACTIVE=prod` 환경변수 적용
- 필요시 각 서비스 Dockerfile/compose 환경변수 수정

## 기타
- 빌드 캐시/속도 개선을 위해 lock파일(pnpm/yarn 등) 및 빌드 산출물 경로 확인 필요
- 프론트엔드 nginx.conf에서 API 경로/정적 라우팅 커스터마이즈 가능
