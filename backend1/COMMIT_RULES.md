# 커밋 규칙 (권장)

프로젝트에서 일관된 커밋 메시지 스타일을 사용하면 변경 이력 관리가 쉬워집니다. 아래는 Conventional Commits(간소화) 규칙입니다.

형식:
```
<type>(<scope>): <subject>

<body>

<footer>
```

- type: feat, fix, docs, style, refactor, test, chore
- scope: 변경한 모듈/패키지(선택)
- subject: 한 줄 요약(명령형, 50자 이내)
- body: 상세 설명(선택)
- footer: 이슈 번호나 BREAKING CHANGE

예시:
```
feat(auth): add JWT token refresh endpoint

Add refresh-token endpoint to renew access tokens without re-login.

Closes #12
```

간단한 Git 사용 예시:
- 스테이지: git add <파일>
- 커밋: git commit -m "feat(auth): add JWT token refresh endpoint"
- 푸시: git push origin main

추천 도구:
- 커밋 메시지 템플릿 파일 사용: `.gitmessage` 생성 후 `git config commit.template .gitmessage`
- 훅으로 메시지 검사: Husky(프론트/Node) 또는 로컬 스크립트

---

## 이슈 기반 개발 권장 흐름
기능 개발은 기능 단위로 GitHub 이슈를 생성하고, 해당 이슈를 기준으로 여러 개의 작은 커밋으로 변경을 나눠 진행하세요. 이렇게 하면 코드 리뷰와 롤백이 쉬워집니다.

1. 이슈 생성
   - 제목은 명확하게(동사 사용): "Add user profile API"
   - 본문에 목표, 구현 범위, 완료 기준(Definition of Done)을 작성합니다.
   - 필요한 경우 하위 작업(checkbox)으로 세분화합니다.

2. 브랜치 생성
   - 브랜치명 규칙: `feature/<issue-number>-short-description` 또는 `fix/<issue-number>-short-description`
   - 예: `feature/12-user-profile-api`

3. 작은 단위 커밋으로 개발
   - 각 커밋은 자족적(self-contained)이어야 하며, 한 가지 변경만 포함하세요(예: 모델 추가, 서비스 로직, 테스트 추가).
   - 커밋 메시지는 위의 Conventional Commit 규칙을 따르며, 본문에 필요한 설명을 추가합니다.
   - 예시 커밋 분할:
     - `feat(user): add User entity and repository`
     - `feat(user): implement user service createUser`
     - `test(user): add unit tests for createUser`
     - `refactor(user): simplify user DTO mapping`

4. PR(Pull Request) 생성
   - PR 제목에 이슈 번호 포함: `#12 Add user profile API`
   - PR 본문에 이슈 링크 및 변경 사항 요약, 테스트 방법을 작성하세요.
   - 코드 리뷰에서 피드백을 반영할 때는 작은 커밋으로 추가하세요.

5. Merge 전략
   - Squash merge 또는 Merge commit 정책은 팀 합의에 따릅니다. (Squash를 쓰면 커밋 로그가 깔끔하지만, 세부 커밋이 사라짐)
   - Merge 시 이슈 닫기: PR 본문에 `Closes #<issue-number>` 추가

---

## 커밋 예시 템플릿
```
<type>(<scope>): <short summary>

Longer description of the change, if necessary.

Closes #<issue-number>
```

원하면 이 템플릿을 `.gitmessage`로 추가하고, 간단한 pre-commit 훅(메시지 형식 검사)을 설정해 드리겠습니다.
