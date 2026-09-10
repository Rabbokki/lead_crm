# ADR-0001: HTML 격리 전략

## 상태
채택

## 배경
운영자가 AI로 생성한 **임의의 HTML**을 등록해 공개 신청 폼으로 렌더링한다.
이 HTML에는 악성 스크립트가 섞일 수 있고(외부 스크립트 로드, `document.cookie`
접근, 관리자 API 호출로 CRM 명단 탈취 등), 실제로 `docs/samples/malicious.html`이
그런 공격 3종을 담고 있다. 업로드 HTML을 그대로 렌더링하면 CRM 명단이 유출된다.

## 결정
업로드 HTML을 **검열해서 지우지 않고**, 브라우저 레벨에서 실행 자체를 차단하는
2단 구조를 택한다.
- 바깥 페이지(`public-form.html`, Thymeleaf): iframe 하나만 렌더링
- 안쪽 콘텐츠(`GET /f/{code}/content`): 업로드 HTML을 `sandbox` iframe 안에서만 렌더링
- iframe: `sandbox="allow-forms allow-scripts"` — **`allow-same-origin` 배제**
- 콘텐츠 응답: `Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline';
  img-src data:; form-action {publicBaseUrl}; frame-ancestors 'self'` + `X-Content-Type-Options: nosniff`
- 공개 제출(`POST /api/public/forms/{id}/submit`)은 **CSRF를 비활성화**한다. sandbox iframe
  (allow-same-origin 없음)의 폼 제출은 `Origin: null`로 전송되어 CSRF 토큰을 안전하게
  전달·검증할 수 없기 때문이다. 관리자(8080)는 CSRF를 유지하며 `/api/**`만 예외로 둔다.
- publicform(8081)은 스프링 시큐리티 기본값 `X-Frame-Options: DENY`를 **비활성화**하고
  클릭재킹 방어를 `CSP frame-ancestors 'self'`로 대체한다. 이 구조는 자기 자신을 iframe에
  넣으므로(동일 출처 self-framing), `DENY`는 신청 완료 화면(submit-success)조차 프레임 안에서
  렌더링하지 못하게 막는다. `frame-ancestors 'self'`는 self-framing은 허용하면서 외부
  사이트의 프레이밍은 차단하는 X-Frame-Options의 상위 호환이다. 관리자(8080)는
  `X-Frame-Options: DENY`를 그대로 유지한다.

## 근거
- 블랙리스트 검열(태그 제거)은 우회가 쉽다. sandbox + CSP는 화이트리스트 방식이라
  "무엇을 허용할지"만 정의하면 나머지는 브라우저가 전부 차단한다.
- `allow-same-origin`을 빼면 iframe 문서는 불투명 출처(opaque origin)가 되어
  `document.cookie` 접근·동일 출처 자격증명 요청이 원천 차단된다.
- `default-src 'none'`으로 외부 스크립트·fetch·이미지가 모두 막히므로, 명단을
  외부로 보내거나 관리자 API를 호출하는 경로가 사라진다. 폼 제출만 `form-action`으로 허용.
- 대안(HTML sanitizer 라이브러리로 스크립트 제거)은 정상 마크업까지 훼손하거나
  신규 우회 벡터에 계속 노출된다. 실행 차단이 더 견고하다.
- **브라우저 실측 검증**: `docs/samples/malicious.html`을 실제 등록해 확인한 결과, 인라인
  `<script>`는 실행 단계에 도달하지 못하고 CSP가 차단했다("Executing inline script violates
  the following Content Security Policy directive: default-src 'none' ... The action has been
  blocked."). 외부 스크립트 로드도 `script-src-elem` fallback으로 차단됐다. 업로드 HTML에서
  악성 코드를 **제거하지 않아도 실행 자체가 봉쇄**되므로, "검열·정제 대신 브라우저 레벨
  차단"이라는 이 결정이 실제로 성립함을 보여준다.

## 한계 / 후속
- **로컬 포트 분리(8080/8081)는 오리진만 나누고 쿠키는 격리하지 못한다.** 쿠키는
  도메인 단위로만 분리되므로 `localhost:8080`과 `localhost:8081`은 쿠키를 공유한다.
  즉 포트 분리는 방어의 본체가 아니며, 실제 격리는 **CSP + sandbox**가 담당한다.
- 운영 환경에서는 `admin.example.com` / `forms.example.com`처럼 **호스트를 분리**해
  쿠키까지 격리해야 한다. `AppProperties`의 base URL을 그 호스트로 바꾸면 된다.
- 격리 관련 코드를 수정하면 `PublicFormIsolationTest`(malicious.html 실제 업로드)를
  반드시 다시 돌린다.
- 공개 제출의 CSRF 비활성화는 **제3자가 공개 제출 엔드포인트로 임의 데이터를 넣을 수 있음**을
  감수한 것이다. 제출은 신청 데이터 생성에 국한되며 인증·권한을 바꾸지 않는다. 운영
  환경에서는 rate limiting·CAPTCHA·honeypot 필드 등으로 스팸 유입을 보완한다.
