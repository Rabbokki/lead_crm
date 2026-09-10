# ADR-0008: 테스트 컨텍스트 전략 — 프로파일 분리 대응

## 상태
채택

## 배경
보안·컨트롤러가 `@Profile("admin")` / `@Profile("publicform")`로 분리되어(ADR-0005),
관리자 컨트롤러와 공개 폼 컨트롤러가 **한 스프링 컨텍스트에 동시에 뜨지 않는다.**
그런데 성공 흐름 E2E는 "관리자로 생성 → 공개로 방문·제출 → 관리자로 통계 확인"이라
양쪽을 모두 건드린다. 운영 코드의 `@Profile` 값은 바꾸지 않는다는 제약이 있다.

## 결정
테스트를 두 클래스로 나누고, 서비스 계층이 프로파일 무관(`@Service`)인 점을 활용한다.
- `AdminE2ETest` (`@ActiveProfiles("admin")`): 관리자 API는 MockMvc로 호출하고,
  공개 폼의 방문/제출은 프로파일 무관 빈 `PublicFormService`를 직접 호출해 재현한다.
  통계까지 실제 수치를 단정(visits=5, visitors=5, submissions=2, 전환율=40.0 등).
  미인증 접근 401도 여기서 검증.
- `PublicFormIsolationTest` (`@ActiveProfiles("publicform")`): 격리·실패 흐름을
  공개 엔드포인트(MockMvc)로 실검증 — 404, 422, CSP/sandbox(malicious.html 실제 업로드),
  동일 vid 3회 방문. 데이터 준비는 프로파일 무관 서비스로 수행.
- 공용 `AbstractIntegrationTest`: **PostgreSQL 16 컨테이너를 정적으로 한 번만** 띄워
  컨텍스트가 갈려도 공유(Testcontainers). dev DB(leadcrm)는 건드리지 않는다.
- 각 테스트는 `@BeforeEach`에서 도메인 테이블을 정리해 상호 독립성을 확보한다.

## 근거
- 운영 `@Profile`을 바꾸지 않으면서도 E2E의 크로스-프로파일 흐름을 온전히 검증할 수 있다.
  서비스가 프로파일 무관이라 컨트롤러 없이도 동일 로직을 정확히 재현한다.
- 격리 검증(CSP 헤더·iframe sandbox)은 반드시 실제 공개 엔드포인트를 거쳐야 의미가 있어
  공개 프로파일 컨텍스트에서 MockMvc로 확인한다.
- 컨테이너 단일화로 테스트 시간을 줄인다.

## 한계 / 후속
- E2E의 공개 흐름은 컨트롤러(쿠키 발급 등)가 아니라 서비스를 직접 호출하므로,
  컨트롤러 계층의 방문 기록·쿠키 로직은 `PublicFormIsolationTest`가 별도로 담보한다.
- `ObjectMapper`는 컨텍스트 빈으로 노출되지 않아 테스트에서 직접 생성해 쓴다.
