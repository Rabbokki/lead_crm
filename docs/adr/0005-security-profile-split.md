# ADR-0005: 보안 설정 프로파일 분리 + 소유권 위반 404 통일

## 상태
채택

## 배경
관리자 서버(8080)와 공개 폼 서버(8081)는 같은 코드베이스를 프로파일로 나눠 띄운다.
초기에는 `SecurityConfig` 하나가 두 프로파일에 공통 적용되어, **공개 폼 서버에도
formLogin과 `AdminUserDetailsService`가 로드되어 users 테이블을 조회**했다.
공개 폼 서버에는 관리자 인증 기능이 존재할 이유가 없다(공격 표면만 늘어난다).

## 결정
보안 설정을 3개로 분리한다.
- `CommonSecurityConfig` (프로파일 무관): `PasswordEncoder`만 — 관리자 로그인과
  계정 시딩(양쪽 프로파일) 모두 필요하므로 공용화.
- `AdminSecurityConfig` (`@Profile("admin")`): 기존 그대로 — formLogin,
  `/api/**` 미인증 시 401 엔트리포인트, permitAll 목록, csrf ignoring `/api/**`.
- `PublicSecurityConfig` (`@Profile("publicform")`): formLogin **없음**, 세션 STATELESS,
  `/f/**`·`/api/public/**`만 permitAll, 그 외 전부 `denyAll`, csrf 비활성(공개 제출용).
- `AdminUserDetailsService`에 `@Profile("admin")` 부여 → 8081에는 로드되지 않음.

또한 **소유권 위반은 403이 아니라 404**(`NotFoundException`)로 통일한다.
타인 소유 리소스 접근 시 존재 여부 자체를 노출하지 않는다.

## 근거
- 공개 폼 서버에서 인증 코드·엔드포인트를 제거하면 CRM 명단을 노리는 공격 표면이 줄고,
  8081의 `/login`·`/admin/**`·`/api/admin/**`이 전부 **403**으로 막힌다(실측 확인).
- `PasswordEncoder`는 인코딩 알고리즘일 뿐 인증 흐름이 아니므로 공용화해도 안전하다.
- 404 통일은 "이 ID의 캠페인이 남의 것"이라는 사실조차 감춰 열거 공격을 어렵게 한다.
- 인증은 과제 스택대로 **Spring Security 세션 쿠키**를 쓴다. 서버가 상태를 쥐는 단순
  모델이라 관리자 3화면 + JSON API에 충분하고, 토큰 회전·저장 등 복잡도를 피한다.
- 로그인은 **Spring Security 표준 formLogin**을 그대로 쓴다: `POST /login`,
  `application/x-www-form-urlencoded`, 파라미터 `email`/`password`. 성공 시 세션 쿠키를
  발급하고 `/admin/campaigns`로 리다이렉트한다. 관리자 UI가 **SPA가 아니라 Thymeleaf
  서버 렌더링**이므로, 브라우저 폼 제출 → 세션 쿠키라는 표준 흐름이 가장 자연스럽다.
- 별도 `POST /api/auth/login` **JSON 엔드포인트는 만들지 않았다.** 토큰을 클라이언트가
  받아 보관·전송하는 방식은 SPA/모바일에 맞는 모델인데, 여기서는 소비자가 서버 렌더링
  화면뿐이라 이점이 없고 CSRF·토큰 저장 등 표면만 늘린다. 표준 formLogin 하나로 화면
  로그인과 세션 기반 API 인증을 모두 커버한다.
- publicform은 앞의 공개 경로에 더해 **`/error`도 permitAll에 포함**한다. `/error`가
  `denyAll`로 떨어지면 1차 오류 처리 중 **2차 403**이 발생해 원인 파악이 어려워진다(실제로
  공개 폼 제출 실패를 디버깅할 때 이 문제로 시간이 소요됐다). `/error`는 에러 상세를
  노출하지 않으므로 permitAll이어도 안전하다.
- `AdminUserDetailsService`에 `@Profile("admin")`을 붙인 결과 publicform에는
  `UserDetailsService` 빈이 사라져, 스프링 부트가 기본 인메모리 계정을 자동 생성했다
  ("Using generated security password" 로그). 공개 폼 서버에는 인증 인프라가 존재할 이유가
  없으므로 `application.yml`의 publicform 프로파일에서만 `spring.autoconfigure.exclude`로
  `UserDetailsServiceAutoConfiguration`을 제외했다. admin 프로파일은 영향받지 않는다.

## 한계 / 후속
- 세션 쿠키는 오리진이 아니라 도메인 단위라 로컬 포트 분리로는 8080/8081이 쿠키를
  공유한다(→ ADR-0001). 운영에서는 호스트 분리로 세션 쿠키까지 격리한다.
- admin의 permitAll 목록은 격리의 경계이므로 확장/축소하지 않는다.
