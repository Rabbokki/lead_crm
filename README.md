# 리드마그넷 CRM 운영 시스템

## 사전 요구사항

- JDK 21
- Docker (PostgreSQL 16 및 테스트용 Testcontainers)

## 실행 방법

1. 환경 변수 파일 준비

```bash
   cp .env.example .env
```

Windows PowerShell:

```powershell
   Copy-Item .env.example .env
```

2. 데이터베이스 기동

```bash
   docker compose up -d
```

3. 애플리케이션 실행 — 관리자와 공개 폼을 각각 실행한다(서로 다른 프로파일·포트).

```bash
   ./gradlew bootRun --args='--spring.profiles.active=admin'
```

```bash
   ./gradlew bootRun --args='--spring.profiles.active=publicform'
```

### 접속

- 관리자: http://localhost:8080/login
- API 문서(Swagger): http://localhost:8080/swagger-ui.html
- 공개 폼: http://localhost:8081/f/{code} (관리자에서 링크 발급 후 접속)

초기 관리자 계정은 `.env`의 `ADMIN_EMAIL` / `ADMIN_PASSWORD`
(기본값 `admin@glowuprizz.com` / `admin1234`). 최초 실행 시 자동 생성된다.

## 테스트 방법

Docker가 실행 중이어야 한다(Testcontainers가 PostgreSQL 컨테이너를 띄운다).

```bash
./gradlew test
```