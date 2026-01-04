# allinfluencer-backend (Spring Boot)

NestJS 기반 `apps/api`와 별도로 운영/이관을 위한 Spring Boot 백엔드 프로젝트입니다.

## 아키텍처

- **DDD**: `domain`(추후), `application`, `infrastructure`, `presentation` 레이어로 분리
- **CQRS**:
  - **Query(Read model)**: MyBatis (복잡한 조회/조인/페이지네이션에 유리)
  - **Command(Write model)**: JPA (추후 Command/use-case 확장 시 적용)

## 실행

Java 21이 필요합니다.

```bash
cd allinfluencer-backend
mvn spring-boot:run
```

기본 포트는 `3001` 입니다.

## 환경변수 (로컬 Postgres)

- `SERVER_PORT` (기본: 3001)
- `DATABASE_URL` (기본: `jdbc:postgresql://localhost:5432/allinfluencer`)
- `DATABASE_USERNAME` (기본: allinfluencer)
- `DATABASE_PASSWORD` (기본: allinfluencer)
- `JWT_ISSUER` (기본: all-influencer)
- `JWT_SECRET` (기본: dev-secret-change-me)

## 헬스체크

- `GET /health` (인증 불필요)

## (MVP) 사업자 마이페이지 조회 API

NestJS의 `my/advertiser` 라우팅과 동일한 형태로 맞춰두었습니다.

- `GET /my/advertiser/job-posts?status=&cursor=&limit=`
- `GET /my/advertiser/job-posts/{jobPostId}/offers?status=&cursor=&limit=`

## (MVP) 사업자 마이페이지 Command API

- `POST /my/advertiser/job-posts`
- `POST /my/advertiser/job-posts/{jobPostId}/offers`
- `PATCH /my/advertiser/offers/{offerId}/expire`
- `PATCH /my/advertiser/applications/{applicationId}/status`

## (이관 진행중) Auth / Users / Influencer MyPage

- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /auth/logout-all`
- `GET /auth/me`
- `GET /auth/sessions`
- `POST /auth/local/signup` *(TODO: /auth/signup과 동일 로직으로 연결 예정)*
- `POST /auth/local/login` *(TODO: /auth/login과 동일 로직으로 연결 예정)*
- `GET /auth/google|kakao|naver` *(TODO)*
- `GET /auth/google|kakao|naver/callback` *(TODO)*
- `GET /auth/link` *(TODO)*
- `DELETE /auth/link/{provider}` *(TODO)*
- `GET /my` (role 기반 `{ url }` 반환)
- `GET /my/influencer/*` (컨트롤러 라우트 생성 완료, 서비스 구현은 진행중)
- `GET /users` (public)
- `POST/PATCH/DELETE /users/*` (진행중)

## 레이어드(DDD/CQRS) 관점에서의 구성

- **presentation**: HTTP 요청/응답 DTO + Controller (얇게 유지)
- **application**
  - **command**: 유스케이스(트랜잭션) + command model
  - **query**: read-model 조회/페이지네이션
  - **port(out)**: application이 의존하는 외부(저장소/조회) 인터페이스
- **infrastructure**
  - **adapter**: port(out) 구현체 (현재는 MyBatis 기반)
  - **mybatis**: Mapper/SQL/typehandler

> 현재 단계는 “이관 가능한 골격”을 목표로 해서 **domain(aggregate/value object)** 는 최소화했습니다.
> 도메인 규칙(상태전이, 정책)이 늘어나는 시점에 domain 레이어를 두고 command 쪽에 흡수시키는 것을 권장합니다.

## 빌더 패턴 사용 판단

- **요청/응답 DTO**: record + validation으로 충분 (필드가 적고 직렬화/역직렬화가 단순)
- **도메인 객체/복잡한 조립**: 필드가 많고 선택 값이 많은 경우에만 builder(또는 static factory)를 권장

> 인증은 우선 `Authorization: Bearer <JWT>` 를 파싱하는 최소 필터만 제공하며,
> NestJS 토큰 규격과 100% 호환은 추후 이관 단계에서 맞추는 것을 권장합니다.

## 회원가입 확장(역할별 필수 입력)

- **INFLUENCER**
  - `categories`(전문분야) 필수
  - `channels`(SNS 채널) 입력
  - 가입 시 `influencer_profiles`, `channels` 생성 + `influencer_verifications(status=PENDING)` 생성
  - **ADMIN 승인 전까지 로그인/토큰 발급 불가** (status=INACTIVE)
- **ADVERTISER**
  - `companyName`, `industry`, `businessRegistrationNumber` 필수
  - 가입 시 `advertiser_companies` 저장 + `advertiser_verifications(status=PENDING)` 생성

## 관리자 승인 API (인플루언서)

- `PATCH /admin/influencers/{userId}/approve` (ROLE_ADMIN)
- `PATCH /admin/influencers/{userId}/reject` (ROLE_ADMIN)


