# API 문서

> 실행 후 `http://localhost:8080/swagger-ui.html` 에서도 확인할 수 있다(springdoc-openapi).
> 이 문서는 저장소만으로 API를 파악할 수 있도록 정리한 요약이다.

- 관리자 API(`/api/admin/**`, `/admin/**`)는 **인증 필수**, 8080(admin 프로파일) 전용.
- 공개 API(`/f/**`, `/api/public/**`)는 **인증 없음**, 8081(publicform 프로파일) 전용.
- 공통 에러: 미인증 관리자 API → **401**, 없는 링크 코드/타인 소유 리소스 → **404**,
  필수 필드 누락·검증 실패 → **422**.

## 인증 (Spring Security 세션 + 폼 로그인)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/login` | 불필요 | 관리자 로그인 페이지 |
| POST | `/login` | 불필요 | 폼 로그인. 파라미터 `email`, `password`(x-www-form-urlencoded). 성공 시 `/admin/campaigns`로 리다이렉트, 세션 쿠키 발급 |
| POST | `/logout` | 세션 | 로그아웃 |

초기 관리자 계정은 `.env`의 `ADMIN_EMAIL` / `ADMIN_PASSWORD`(기본 `admin@glowuprizz.com` / `admin1234`).

## 관리자 — JSON API (`/api/admin/**`, 인증 필수)

| 메서드 | 경로 | 요청 본문 | 성공 | 응답 |
|---|---|---|---|---|
| POST | `/api/admin/templates` | `{ "name", "rawHtml" }` | 201 | `{ id, name, createdAt }` |
| GET | `/api/admin/templates` | — | 200 | `[{ id, name, createdAt }]` |
| POST | `/api/admin/campaigns` | `{ "name", "description" }` | 201 | `{ id, name, description, createdAt }` |
| GET | `/api/admin/campaigns` | — | 200 | `[{ id, name, description, createdAt }]` |
| GET | `/api/admin/campaigns/{id}` | — | 200 | `{ id, name, description, createdAt }` |
| POST | `/api/admin/campaigns/{id}/forms` | `{ "templateId", "name" }` | 201 | `FormResponse` |
| GET | `/api/admin/campaigns/{id}/forms` | — | 200 | `[FormResponse]` |
| GET | `/api/admin/campaigns/{id}/stats` | — | 200 | `CampaignStats` |
| GET | `/api/admin/campaigns/{id}/stats/channels` | — | 200 | `[ChannelStats]` (4채널 고정) |
| GET | `/api/admin/forms/{id}` | — | 200 | `FormResponse` |
| POST | `/api/admin/forms/{id}/links` | `{ "channel" }` | 201 | `{ id, channel, code, url }` (멱등) |
| GET | `/api/admin/forms/{id}/links` | — | 200 | `[{ id, channel, code, url }]` |
| GET | `/api/admin/forms/{id}/leads` | — | 200 | `[LeadResponse]` |

`channel` enum: `INSTAGRAM`, `X`, `YOUTUBE`, `THREADS`.

**FormResponse**: `{ id, campaignId, templateId, name, fieldSchema:[{name,type,required}], status, createdAt }`
**LeadResponse**: `{ id, channel, visitorId, payload:{…}, createdAt }`
**CampaignStats**: `{ campaignId, campaignName, visits, visitors, submissions, conversionRate }`
**ChannelStats**: `{ channel, visits, visitors, submissions, conversionRate }`

### 요청/응답 예시

```
POST /api/admin/forms/12/links
Content-Type: application/json
{ "channel": "INSTAGRAM" }

201 Created
{ "id": 34, "channel": "INSTAGRAM", "code": "a1b2c3d4",
  "url": "http://localhost:8081/f/a1b2c3d4" }
```

```
GET /api/admin/campaigns/7/stats
200 OK
{ "campaignId": 7, "campaignName": "9월 캠페인",
  "visits": 5, "visitors": 5, "submissions": 2, "conversionRate": 40.0 }
```

```
GET /api/admin/campaigns/7/stats/channels
200 OK
[ { "channel": "INSTAGRAM", "visits": 3, "visitors": 3, "submissions": 1, "conversionRate": 33.3 },
  { "channel": "X",         "visits": 0, "visitors": 0, "submissions": 0, "conversionRate": 0.0 },
  { "channel": "YOUTUBE",   "visits": 2, "visitors": 2, "submissions": 1, "conversionRate": 50.0 },
  { "channel": "THREADS",   "visits": 0, "visitors": 0, "submissions": 0, "conversionRate": 0.0 } ]
```

## 관리자 — 화면 (`/admin/**`, Thymeleaf, 인증 필수)

화면용 POST는 CSRF 토큰이 필요하다(Thymeleaf `th:action` 폼에 자동 주입).

| 메서드 | 경로 | 파라미터 | 동작 |
|---|---|---|---|
| GET | `/admin/campaigns` | — | 캠페인 목록 + 요약 통계 |
| POST | `/admin/campaigns` | `name`, `description` | 캠페인 생성 후 목록으로 리다이렉트 |
| POST | `/admin/templates` | `name`, `rawHtml` | 템플릿 등록 후 목록으로 리다이렉트 |
| GET | `/admin/campaigns/{id}` | — | 캠페인 상세(채널별·폼·링크·신청 명단) |
| POST | `/admin/campaigns/{id}/forms` | `templateId`, `name` | 폼 생성 후 상세로 리다이렉트 |
| POST | `/admin/forms/{formId}/links` | `campaignId` | 채널 4개 링크 일괄 발급 후 상세로 리다이렉트 |

## 공개 (`/f/**`, `/api/public/**`, 인증 없음, 8081)

| 메서드 | 경로 | 설명 | 비고 |
|---|---|---|---|
| GET | `/f/{code}` | 신청 폼 바깥 페이지(iframe). 방문 1건 기록, `vid` 쿠키 발급 | 없는 코드 → 404 |
| GET | `/f/{code}/content` | iframe 안 콘텐츠(업로드 HTML) | 응답에 `Content-Security-Policy: default-src 'none' …` 헤더 |
| POST | `/api/public/forms/{formId}/submit` | 신청 제출(x-www-form-urlencoded). 폼 필드 스키마 화이트리스트로 저장 | 필수 필드 누락 → 422 |

```
POST /api/public/forms/12/submit
Content-Type: application/x-www-form-urlencoded
name=김철수&email=chulsoo@example.com&__link=a1b2c3d4
→ 신청 완료 페이지
```
