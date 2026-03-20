# Piano Practice Companion - 기술 아키텍처

**문서 버전**: 1.0
**작성일**: 2026-03-19
**상태**: Draft

---

## 목차

1. [기술 스택 선정 및 근거](#1-기술-스택-선정-및-근거)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [DB 스키마 (ERD)](#3-db-스키마-erd)
4. [API 설계 (RESTful)](#4-api-설계-restful)
5. [프로젝트 구조](#5-프로젝트-구조)
6. [인증/인가 방식](#6-인증인가-방식)
7. [MVP 개발 우선순위](#7-mvp-개발-우선순위)

---

## 1. 기술 스택 선정 및 근거

### 1.1 전체 기술 스택 요약

| 계층 | 기술 | 버전 |
|------|------|------|
| **프론트엔드** | Next.js (React) | 15.x |
| **백엔드** | Kotlin + Spring Boot | Kotlin 2.x, Spring Boot 3.4.x |
| **데이터베이스** | PostgreSQL | 16.x |
| **ORM** | Spring Data JPA + Kotlin JDSL | JDSL 3.x |
| **인증** | Spring Security + JWT | - |
| **캐시** | Redis (Upstash) | 7.x |
| **이미지 저장소** | AWS S3 (또는 Cloudflare R2) | - |
| **컨테이너** | Docker + Docker Compose | - |
| **CI/CD** | GitHub Actions | - |
| **모니터링** | Sentry + Spring Actuator | - |
| **API 문서** | SpringDoc OpenAPI (Swagger) | - |

### 1.2 프론트엔드: Next.js 15 (React)

**선정 근거:**

- **웹 앱 우선 전략**: PRD의 타겟 사용자(20~50대 성인)는 데스크탑/모바일 웹 접근이 자연스러움. 웹 우선으로 빠르게 MVP를 출시하고 검증한 후, PWA 또는 React Native로 네이티브 앱 전환 가능
- **서버 사이드 렌더링(SSR)**: 초기 로딩 속도 3초 이내 목표(PRD 6.1) 충족에 유리. App Router의 서버 컴포넌트로 클라이언트 번들 최소화
- **SEO 대응**: 향후 공개 프로필이나 공유 기능 추가 시 SSR/SSG로 SEO 확보
- **풍부한 에코시스템**: 차트 라이브러리(Recharts), 캘린더 히트맵, 타이머 등 UI 요구사항에 맞는 React 라이브러리 다수
- **Vercel 배포**: Next.js에 최적화된 Vercel 플랫폼으로 무중단 배포, Edge Functions, 이미지 최적화 제공

**프론트엔드 주요 라이브러리:**

| 목적 | 라이브러리 | 근거 |
|------|-----------|------|
| 상태 관리 | Zustand | 경량, 보일러플레이트 적음, 타이머 상태 관리에 적합 |
| 서버 상태 | TanStack Query (React Query) | API 캐싱, 낙관적 업데이트, 오프라인 지원 |
| UI 컴포넌트 | shadcn/ui + Tailwind CSS | 커스터마이징 자유도 높음, 디자인 시스템과 잘 맞음 |
| 차트 | Recharts | 주간/월간 통계 막대 차트, 곡별 비율 차트 |
| 날짜 | date-fns | 경량, 트리쉐이킹 지원 |
| 폼 관리 | React Hook Form + Zod | 레슨 노트/곡 등록 폼 검증 |
| 아이콘 | Lucide React | 일관된 아이콘 스타일 |

### 1.3 백엔드: Kotlin + Spring Boot 3

**선정 근거:**

- **Kotlin**: Null Safety로 NPE 방지, data class로 불변 DTO 패턴, 확장 함수로 코드 간결화
- **Spring Boot 3**: 프로덕션 레디 프레임워크. Spring Security, Spring Data JPA, Actuator 등 필요한 모듈 통합
- **GraalVM Native 지원**: 향후 콜드 스타트 최적화를 위한 네이티브 이미지 빌드 가능
- **풍부한 에코시스템**: 캐시, 보안, 모니터링, API 문서화 등 엔터프라이즈급 기능 즉시 활용

### 1.4 데이터베이스: PostgreSQL 16

**선정 근거:**

- **관계형 데이터 모델**: 사용자-연습세션-곡-레슨노트 간 복잡한 관계를 자연스럽게 표현
- **JSON 컬럼 지원**: 레슨 노트 메타데이터, 과제 체크리스트 등 반정형 데이터 저장에 jsonb 활용
- **윈도우 함수**: 스트릭 계산, 주별/월별 통계 집계에 강력한 SQL 윈도우 함수 활용
- **확장성**: Supabase 또는 AWS RDS로 관리형 배포, 읽기 전용 레플리카로 수평 확장 가능
- **무료 & 오픈소스**: 라이선스 비용 없음

### 1.5 ORM: Spring Data JPA + Kotlin JDSL

**선정 근거:**

- **Spring Data JPA**: 기본 CRUD, 페이징, 정렬을 인터페이스 선언만으로 자동 생성
- **Kotlin JDSL**: 타입 세이프한 동적 쿼리 작성. 곡 목록 필터/정렬, 연습 통계 집계 등 복잡한 조건 쿼리에 적합. Querydsl 대비 Kotlin 친화적

### 1.6 캐시: Redis

**선정 근거:**

- **세션/토큰 관리**: Refresh Token 저장, 블랙리스트 관리
- **통계 캐싱**: 주간/월간 통계 집계 결과 캐싱으로 반복 쿼리 방지
- **스트릭 계산 캐싱**: 빈번히 조회되는 연속 달성일 캐싱
- **Upstash**: 서버리스 Redis로 관리 오버헤드 최소화, 무료 티어 활용 가능

### 1.7 이미지 저장소: AWS S3 (또는 Cloudflare R2)

**선정 근거:**

- **레슨 노트 이미지**: 악보 사진, 손 자세 사진 등 이미지 첨부(LN-06) 요구사항 충족
- **Presigned URL**: 서버 부하 없이 클라이언트에서 직접 업로드
- **CDN 연동**: CloudFront(S3) 또는 Cloudflare CDN(R2)으로 이미지 전송 최적화
- **사용자당 1GB 제한**(PRD 6.5) 관리 용이

### 1.8 트레이드오프 분석

#### ADR-001: 웹 앱 우선 vs 네이티브 앱

| 항목 | 웹 앱 (Next.js) | 네이티브 (React Native) |
|------|-----------------|----------------------|
| 개발 속도 | 빠름 (단일 코드베이스) | 중간 (플랫폼별 대응 필요) |
| 오프라인 지원 | PWA Service Worker 한계적 | 네이티브 SQLite로 완전 지원 |
| 푸시 알림 | 웹 Push API (iOS 제한적) | 완전 지원 |
| 타이머 백그라운드 | 제한적 (탭 비활성 시 부정확) | 완전 지원 |
| 앱 스토어 배포 | TWA/PWA로 가능하나 제한적 | 완전 지원 |
| 접근성 | URL로 즉시 접근 | 설치 필요 |

**결정**: 웹 앱 우선. MVP에서 핵심 기능을 검증한 후 Phase 2~3에서 React Native 또는 Capacitor로 네이티브 전환. 타이머는 Page Visibility API + Web Worker로 정확도 확보.

#### ADR-002: 모놀리식 vs 마이크로서비스

**결정**: 모놀리식 아키텍처. MVP 단계에서 사용자 수가 적고 도메인이 단순하므로 모놀리식으로 빠르게 개발. 도메인별 패키지 구조로 경계를 명확히 하여 향후 분리 용이하게 설계.

---

## 2. 시스템 아키텍처

### 2.1 전체 시스템 구성도

```
┌─────────────────────────────────────────────────────────────────────┐
│                           클라이언트                                │
│                                                                     │
│  ┌──────────────────┐    ┌──────────────────┐                      │
│  │  웹 브라우저       │    │  모바일 (PWA)     │                      │
│  │  (Next.js SSR)    │    │  홈스크린 추가     │                      │
│  └────────┬─────────┘    └────────┬─────────┘                      │
│           │                       │                                 │
└───────────┼───────────────────────┼─────────────────────────────────┘
            │ HTTPS                 │ HTTPS
            ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Vercel (프론트엔드)                          │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Next.js 15 App                                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │  │
│  │  │ Server       │  │ API Routes   │  │ Static       │      │  │
│  │  │ Components   │  │ (BFF 역할)   │  │ Assets       │      │  │
│  │  └──────────────┘  └──────┬───────┘  └──────────────┘      │  │
│  └───────────────────────────┼──────────────────────────────────┘  │
│                              │                                      │
└──────────────────────────────┼──────────────────────────────────────┘
                               │ HTTPS (REST API)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   백엔드 (Cloud Run / Railway)                      │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Spring Boot 3 Application                                   │  │
│  │                                                              │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │  │
│  │  │ Auth     │  │ Practice │  │ Lesson   │  │ Piece    │   │  │
│  │  │ Module   │  │ Module   │  │ Module   │  │ Module   │   │  │
│  │  ├──────────┤  ├──────────┤  ├──────────┤  ├──────────┤   │  │
│  │  │ Goal     │  │ Timeline │  │ Stats    │  │ User     │   │  │
│  │  │ Module   │  │ Module   │  │ Module   │  │ Module   │   │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │  │
│  │                                                              │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │  공통: Security Filter / Exception Handler / Logging   │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└────────┬───────────────────┬───────────────────┬────────────────────┘
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  PostgreSQL 16  │ │  Redis          │ │  AWS S3         │
│  (Supabase /    │ │  (Upstash)      │ │  (이미지)       │
│   AWS RDS)      │ │                 │ │                 │
│                 │ │  - JWT 블랙리스트│ │  - 레슨 노트    │
│  - 사용자       │ │  - 통계 캐시    │ │    이미지       │
│  - 연습 세션    │ │  - 스트릭 캐시  │ │  - Presigned    │
│  - 레슨 노트   │ │  - Rate Limit   │ │    URL 업로드   │
│  - 곡 관리     │ │                 │ │                 │
│  - 목표/통계   │ │                 │ │                 │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 2.2 요청 흐름

```
사용자 → Next.js (SSR/CSR) → Spring Boot API → PostgreSQL
                                      ↕
                                    Redis (캐시)
                                      ↕
                                    S3 (이미지)
```

**일반적인 API 호출 흐름:**

```
1. 클라이언트 요청
   │
2. Next.js API Route (BFF) - 토큰 관리, 요청 조합
   │
3. Spring Boot Controller
   │  - @Valid 입력 검증
   │  - Spring Security 인증/인가
   │
4. Service Layer
   │  - 비즈니스 로직
   │  - 트랜잭션 관리
   │
5. Repository Layer
   │  - JPA 기본 쿼리
   │  - Kotlin JDSL 동적 쿼리
   │
6. PostgreSQL
   │
7. 응답 반환 (View/DTO 변환)
```

### 2.3 배포 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                    GitHub Repository                 │
│                                                     │
│  main branch push/merge                             │
│         │                                           │
│         ▼                                           │
│  ┌─────────────────────────────────────────────┐   │
│  │           GitHub Actions CI/CD              │   │
│  │                                             │   │
│  │  1. Lint & Format Check (ktlint)            │   │
│  │  2. Unit Tests (Kotest + MockK)             │   │
│  │  3. Integration Tests (TestContainers)      │   │
│  │  4. Build Docker Image                      │   │
│  │  5. Deploy                                  │   │
│  └──────────────┬──────────────────────────────┘   │
│                 │                                    │
│        ┌────────┴────────┐                          │
│        ▼                 ▼                          │
│  ┌──────────┐     ┌──────────┐                     │
│  │ Vercel   │     │ Cloud Run│                     │
│  │ (FE)     │     │ (BE)     │                     │
│  └──────────┘     └──────────┘                     │
└─────────────────────────────────────────────────────┘
```

---

## 3. DB 스키마 (ERD)

### 3.1 ERD 다이어그램

```
┌──────────────────┐       ┌──────────────────────────┐
│     users         │       │   practice_sessions       │
├──────────────────┤       ├──────────────────────────┤
│ PK id            │───┐   │ PK id                    │
│    email          │   │   │ FK user_id               │──→ users.id
│    password_hash  │   │   │    started_at            │
│    name           │   │   │    ended_at              │
│    profile_image  │   │   │    total_duration_seconds│
│    experience_level│   │   │    memo                  │
│    daily_goal_min │   │   │    mood                  │
│    weekly_goal_days│  │   │    created_at            │
│    weekly_goal_min│   │   │    updated_at            │
│    provider       │   │   └─────────────┬────────────┘
│    provider_id    │   │                 │
│    created_at     │   │                 │ 1:N
│    updated_at     │   │                 ▼
│    deleted_at     │   │   ┌──────────────────────────┐
└──────────────────┘   │   │ practice_session_pieces   │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK session_id             │──→ practice_sessions.id
                       │   │ FK piece_id               │──→ pieces.id
                       │   │    duration_seconds       │
                       │   │    order_index            │
                       │   │    created_at             │
                       │   └──────────────────────────┘
                       │
                       │   ┌──────────────────────────┐
                       ├──→│        pieces              │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK user_id               │──→ users.id
                       │   │    title                  │
                       │   │    composer               │
                       │   │    genre                  │
                       │   │    difficulty             │  (1~5)
                       │   │    status                 │  (NOT_STARTED/PRACTICING/
                       │   │                           │   FINISHING/COMPLETED/ON_HOLD)
                       │   │    progress_percent       │  (0~100)
                       │   │    memo                   │
                       │   │    started_at             │
                       │   │    completed_at           │
                       │   │    created_at             │
                       │   │    updated_at             │
                       │   │    deleted_at             │
                       │   └─────────────┬────────────┘
                       │                 │
                       │                 │ 1:N
                       │                 ▼
                       │   ┌──────────────────────────┐
                       │   │  piece_section_memos      │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK piece_id              │──→ pieces.id
                       │   │    section_label          │  (예: "마디 17~24")
                       │   │    content                │
                       │   │    created_at             │
                       │   │    updated_at             │
                       │   └──────────────────────────┘
                       │
                       │   ┌──────────────────────────┐
                       ├──→│     lesson_notes          │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK user_id               │──→ users.id
                       │   │    lesson_number          │
                       │   │    lesson_date            │
                       │   │    start_time             │
                       │   │    end_time               │
                       │   │    content                │
                       │   │    teacher_feedback       │
                       │   │    created_at             │
                       │   │    updated_at             │
                       │   │    deleted_at             │
                       │   └─────────────┬────────────┘
                       │                 │
                       │            1:N  │  1:N
                       │        ┌────────┴────────┐
                       │        ▼                  ▼
                       │ ┌───────────────┐ ┌───────────────────┐
                       │ │lesson_         │ │lesson_note_pieces │
                       │ │assignments     │ ├───────────────────┤
                       │ ├───────────────┤ │ PK id             │
                       │ │ PK id         │ │ FK lesson_note_id │→lesson_notes.id
                       │ │ FK lesson_    │ │ FK piece_id       │→pieces.id
                       │ │    note_id    │ └───────────────────┘
                       │ │ content       │
                       │ │ is_completed  │
                       │ │ order_index   │
                       │ │ created_at    │
                       │ │ updated_at    │
                       │ └───────────────┘
                       │
                       │   ┌──────────────────────────┐
                       ├──→│        goals              │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK user_id               │──→ users.id
                       │   │ FK piece_id (nullable)   │──→ pieces.id
                       │   │    type                   │  (DAILY_TIME/WEEKLY_DAYS/
                       │   │                           │   WEEKLY_TIME/PIECE_COMPLETION)
                       │   │    target_value           │
                       │   │    target_date (nullable) │
                       │   │    is_active              │
                       │   │    created_at             │
                       │   │    updated_at             │
                       │   └──────────────────────────┘
                       │
                       │   ┌──────────────────────────┐
                       ├──→│   timeline_events         │
                       │   ├──────────────────────────┤
                       │   │ PK id                    │
                       │   │ FK user_id               │──→ users.id
                       │   │ FK piece_id (nullable)   │──→ pieces.id
                       │   │    event_type             │
                       │   │    title                  │
                       │   │    description            │
                       │   │    event_date             │
                       │   │    metadata (jsonb)       │
                       │   │    created_at             │
                       │   └──────────────────────────┘
                       │
                       │   ┌──────────────────────────┐
                       └──→│  lesson_note_images       │
                           ├──────────────────────────┤
                           │ PK id                    │
                           │ FK lesson_note_id        │──→ lesson_notes.id
                           │    image_url              │
                           │    order_index            │
                           │    created_at             │
                           └──────────────────────────┘
```

### 3.2 테이블 상세 정의

#### users (사용자)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 사용자 ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 |
| password_hash | VARCHAR(255) | NULLABLE | 비밀번호 해시 (소셜 로그인 시 null) |
| name | VARCHAR(100) | NOT NULL | 사용자 이름 |
| profile_image_url | VARCHAR(500) | NULLABLE | 프로필 이미지 URL |
| experience_level | VARCHAR(20) | NOT NULL, DEFAULT 'BEGINNER' | BEGINNER / LESSON_STUDENT / RETURNER |
| daily_goal_minutes | INT | NOT NULL, DEFAULT 60 | 일일 목표 (분) |
| weekly_goal_days | INT | NOT NULL, DEFAULT 5 | 주간 목표 (일수) |
| weekly_goal_minutes | INT | NOT NULL, DEFAULT 300 | 주간 목표 (분) |
| provider | VARCHAR(20) | NOT NULL, DEFAULT 'LOCAL' | LOCAL / GOOGLE / APPLE |
| provider_id | VARCHAR(255) | NULLABLE | 소셜 로그인 Provider ID |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |
| deleted_at | TIMESTAMPTZ | NULLABLE | 소프트 삭제일시 |

**인덱스:**
- idx_users_email UNIQUE ON (email) WHERE deleted_at IS NULL
- idx_users_provider ON (provider, provider_id) WHERE provider != 'LOCAL'

#### practice_sessions (연습 세션)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 세션 ID |
| user_id | BIGINT | FK -> users.id, NOT NULL | 사용자 ID |
| started_at | TIMESTAMPTZ | NOT NULL | 연습 시작 시각 |
| ended_at | TIMESTAMPTZ | NULLABLE | 연습 종료 시각 (진행 중이면 null) |
| total_duration_seconds | INT | NOT NULL, DEFAULT 0 | 총 연습 시간 (초) |
| memo | TEXT | NULLABLE | 연습 메모 |
| mood | VARCHAR(20) | NULLABLE | 컨디션 (GREAT/GOOD/OK/BAD/TERRIBLE) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스:**
- idx_practice_sessions_user_started ON (user_id, started_at DESC)
- idx_practice_sessions_user_date ON (user_id, DATE(started_at))

#### practice_session_pieces (세션-곡 매핑)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | ID |
| session_id | BIGINT | FK -> practice_sessions.id, NOT NULL | 세션 ID |
| piece_id | BIGINT | FK -> pieces.id, NOT NULL | 곡 ID |
| duration_seconds | INT | NOT NULL, DEFAULT 0 | 이 곡에 투자한 시간 (초) |
| order_index | INT | NOT NULL, DEFAULT 0 | 곡 연습 순서 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |

#### pieces (곡)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 곡 ID |
| user_id | BIGINT | FK -> users.id, NOT NULL | 사용자 ID |
| title | VARCHAR(200) | NOT NULL | 곡명 |
| composer | VARCHAR(100) | NULLABLE | 작곡가 |
| genre | VARCHAR(50) | NULLABLE | 장르/교재 |
| difficulty | INT | NULLABLE, CHECK(1~5) | 체감 난이도 (1~5) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'NOT_STARTED' | NOT_STARTED/PRACTICING/FINISHING/COMPLETED/ON_HOLD |
| progress_percent | INT | NOT NULL, DEFAULT 0, CHECK(0~100) | 완성도 (%) |
| memo | TEXT | NULLABLE | 메모 |
| started_at | TIMESTAMPTZ | NULLABLE | 연습 시작일 |
| completed_at | TIMESTAMPTZ | NULLABLE | 완성일 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |
| deleted_at | TIMESTAMPTZ | NULLABLE | 소프트 삭제일시 |

#### piece_section_memos (곡 구간별 메모)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | ID |
| piece_id | BIGINT | FK -> pieces.id, NOT NULL | 곡 ID |
| section_label | VARCHAR(100) | NOT NULL | 구간 (예: "마디 17~24") |
| content | TEXT | NOT NULL | 메모 내용 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |

#### lesson_notes (레슨 노트)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 노트 ID |
| user_id | BIGINT | FK -> users.id, NOT NULL | 사용자 ID |
| lesson_number | INT | NOT NULL | 레슨 회차 |
| lesson_date | DATE | NOT NULL | 레슨 날짜 |
| start_time | TIME | NULLABLE | 레슨 시작 시간 |
| end_time | TIME | NULLABLE | 레슨 종료 시간 |
| content | TEXT | NULLABLE | 레슨 내용 |
| teacher_feedback | TEXT | NULLABLE | 선생님 피드백 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |
| deleted_at | TIMESTAMPTZ | NULLABLE | 소프트 삭제일시 |

#### lesson_note_pieces (레슨 노트-곡 연결)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | ID |
| lesson_note_id | BIGINT | FK -> lesson_notes.id, NOT NULL | 레슨 노트 ID |
| piece_id | BIGINT | FK -> pieces.id, NOT NULL | 곡 ID |

UNIQUE constraint on (lesson_note_id, piece_id)

#### lesson_assignments (레슨 과제)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 과제 ID |
| lesson_note_id | BIGINT | FK -> lesson_notes.id, NOT NULL | 레슨 노트 ID |
| content | VARCHAR(500) | NOT NULL | 과제 내용 |
| is_completed | BOOLEAN | NOT NULL, DEFAULT FALSE | 완료 여부 |
| order_index | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |

#### lesson_note_images (레슨 노트 이미지)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | ID |
| lesson_note_id | BIGINT | FK -> lesson_notes.id, NOT NULL | 레슨 노트 ID |
| image_url | VARCHAR(500) | NOT NULL | S3 이미지 URL |
| order_index | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |

#### goals (목표)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 목표 ID |
| user_id | BIGINT | FK -> users.id, NOT NULL | 사용자 ID |
| piece_id | BIGINT | FK -> pieces.id, NULLABLE | 곡 완성 목표 시 곡 ID |
| type | VARCHAR(30) | NOT NULL | DAILY_TIME/WEEKLY_DAYS/WEEKLY_TIME/PIECE_COMPLETION |
| target_value | INT | NOT NULL | 목표값 (분 또는 일수) |
| target_date | DATE | NULLABLE | 곡 완성 목표일 |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | 활성 여부 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 수정일시 |

#### timeline_events (타임라인 이벤트)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGSERIAL | PK | 이벤트 ID |
| user_id | BIGINT | FK -> users.id, NOT NULL | 사용자 ID |
| piece_id | BIGINT | FK -> pieces.id, NULLABLE | 관련 곡 ID |
| event_type | VARCHAR(30) | NOT NULL | PIECE_COMPLETED/LESSON_RECORDED/STREAK_ACHIEVED/GOAL_ACHIEVED/CUSTOM |
| title | VARCHAR(200) | NOT NULL | 이벤트 제목 |
| description | TEXT | NULLABLE | 이벤트 설명 |
| event_date | DATE | NOT NULL | 이벤트 날짜 |
| metadata | JSONB | NULLABLE | 추가 데이터 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | 생성일시 |

### 3.3 관계 요약

```
users (1) --> (N) practice_sessions
users (1) --> (N) pieces
users (1) --> (N) lesson_notes
users (1) --> (N) goals
users (1) --> (N) timeline_events

practice_sessions (1) --> (N) practice_session_pieces
practice_session_pieces (N) <-- (1) pieces

pieces (1) --> (N) piece_section_memos

lesson_notes (1) --> (N) lesson_assignments
lesson_notes (1) --> (N) lesson_note_images
lesson_notes (M) <--> (N) pieces  (via lesson_note_pieces)

goals (N) --> (0..1) pieces
timeline_events (N) --> (0..1) pieces
```

---

## 4. API 설계 (RESTful)

### 4.1 공통 사항

**Base URL**: `/api/v1`

**공통 응답 형식:**

```json
{
  "success": true,
  "data": { ... }
}

{
  "success": true,
  "data": [ ... ],
  "meta": {
    "total": 42,
    "page": 0,
    "size": 20
  }
}

{
  "success": false,
  "error": {
    "code": "PIECE_NOT_FOUND",
    "message": "곡을 찾을 수 없습니다: 123"
  }
}
```

**HTTP 상태 코드:**

| 코드 | 의미 | 사용 |
|------|------|------|
| 200 | OK | 조회, 수정 성공 |
| 201 | Created | 생성 성공 |
| 204 | No Content | 삭제 성공 |
| 400 | Bad Request | 입력 검증 실패 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 중복 리소스 |
| 429 | Too Many Requests | Rate Limit 초과 |
| 500 | Internal Server Error | 서버 오류 |

### 4.2 인증 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/auth/signup | 회원가입 |
| POST | /api/v1/auth/login | 로그인 |
| POST | /api/v1/auth/social | 소셜 로그인 |
| POST | /api/v1/auth/refresh | 토큰 갱신 |
| POST | /api/v1/auth/logout | 로그아웃 |

#### POST /api/v1/auth/signup

Request:
```json
{
  "email": "user@example.com",
  "password": "securePassword123!",
  "name": "김민지",
  "experienceLevel": "LESSON_STUDENT"
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "김민지",
      "experienceLevel": "LESSON_STUDENT"
    }
  }
}
```

#### POST /api/v1/auth/login

Request:
```json
{
  "email": "user@example.com",
  "password": "securePassword123!"
}
```

Response (200): Same as signup

#### POST /api/v1/auth/social

Request:
```json
{
  "provider": "GOOGLE",
  "idToken": "google-id-token..."
}
```

Response (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "user": { ... },
    "isNewUser": false
  }
}
```

#### POST /api/v1/auth/refresh

Request:
```json
{
  "refreshToken": "eyJhbG..."
}
```

Response (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG..."
  }
}
```

### 4.3 연습 기록 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/practice-sessions | 세션 시작 |
| PATCH | /api/v1/practice-sessions/{id}/switch-piece | 곡 전환 |
| PATCH | /api/v1/practice-sessions/{id}/end | 세션 종료 |
| GET | /api/v1/practice-sessions | 세션 목록 |
| GET | /api/v1/practice-sessions/{id} | 세션 상세 |
| GET | /api/v1/practice-sessions/stats/weekly | 주간 통계 |
| GET | /api/v1/practice-sessions/stats/monthly | 월간 통계 |

#### POST /api/v1/practice-sessions (세션 시작)

Request:
```json
{ "pieceId": 1 }
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 100,
    "startedAt": "2026-03-19T18:30:00+09:00",
    "currentPiece": { "id": 1, "title": "체르니 30번 - 8번 연습곡" },
    "totalDurationSeconds": 0
  }
}
```

#### PATCH /api/v1/practice-sessions/{id}/end (세션 종료)

Request:
```json
{
  "memo": "왼손 3-4번 손가락 연결이 아직 부자연스러움",
  "mood": "GOOD"
}
```

Response (200):
```json
{
  "success": true,
  "data": {
    "id": 100,
    "startedAt": "2026-03-19T18:30:00+09:00",
    "endedAt": "2026-03-19T19:15:20+09:00",
    "totalDurationSeconds": 2720,
    "memo": "...",
    "mood": "GOOD",
    "pieces": [
      { "pieceId": 1, "title": "체르니 30번 - 8번", "durationSeconds": 1530 },
      { "pieceId": 2, "title": "하농 - 1번", "durationSeconds": 825 }
    ],
    "dailyGoalProgress": {
      "targetMinutes": 60,
      "achievedMinutes": 45,
      "percent": 75
    }
  }
}
```

#### GET /api/v1/practice-sessions/stats/weekly

Query: ?date=2026-03-19

Response (200):
```json
{
  "success": true,
  "data": {
    "weekStart": "2026-03-17",
    "weekEnd": "2026-03-23",
    "totalDurationSeconds": 16200,
    "practiceDays": 5,
    "dailyStats": [
      { "date": "2026-03-17", "durationSeconds": 2700 },
      { "date": "2026-03-18", "durationSeconds": 3600 }
    ],
    "pieceStats": [
      { "pieceId": 1, "title": "체르니 30번 - 8번", "durationSeconds": 8100, "percent": 50 }
    ],
    "previousWeekDurationSeconds": 14400,
    "changePercent": 12.5
  }
}
```

### 4.4 레슨 노트 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/lesson-notes | 노트 작성 |
| GET | /api/v1/lesson-notes | 노트 목록 |
| GET | /api/v1/lesson-notes/{id} | 노트 상세 |
| PUT | /api/v1/lesson-notes/{id} | 노트 수정 |
| DELETE | /api/v1/lesson-notes/{id} | 노트 삭제 |
| PATCH | /api/v1/lesson-notes/{noteId}/assignments/{id} | 과제 상태 토글 |

#### POST /api/v1/lesson-notes

Request:
```json
{
  "lessonDate": "2026-03-19",
  "startTime": "14:00",
  "endTime": "14:50",
  "content": "체르니 30번 8번: 왼손 아르페지오 부분 정확도 높이기",
  "teacherFeedback": "왼손 4번 손가락 힘 부족, 페달링 타이밍 주의",
  "pieceIds": [1, 3],
  "assignments": [
    { "content": "체르니 30번 8번 17~24마디 느린 템포 반복 연습" },
    { "content": "하농 1번 매일 15분" },
    { "content": "엘리제를 위하여 B파트 악보 읽기" }
  ]
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 13,
    "lessonNumber": 13,
    "lessonDate": "2026-03-19",
    "startTime": "14:00",
    "endTime": "14:50",
    "content": "...",
    "teacherFeedback": "...",
    "pieces": [
      { "id": 1, "title": "체르니 30번 - 8번" },
      { "id": 3, "title": "엘리제를 위하여" }
    ],
    "assignments": [
      { "id": 1, "content": "...", "isCompleted": false }
    ],
    "images": []
  }
}
```

### 4.5 곡 관리 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/pieces | 곡 등록 |
| GET | /api/v1/pieces | 곡 목록 (필터/정렬) |
| GET | /api/v1/pieces/{id} | 곡 상세 |
| PUT | /api/v1/pieces/{id} | 곡 수정 |
| DELETE | /api/v1/pieces/{id} | 곡 삭제 |
| POST | /api/v1/pieces/{id}/section-memos | 구간 메모 추가 |

#### POST /api/v1/pieces

Request:
```json
{
  "title": "체르니 30번 - 8번 연습곡",
  "composer": "Carl Czerny",
  "genre": "클래식",
  "difficulty": 3,
  "status": "PRACTICING",
  "memo": "레슨에서 배우는 곡"
}
```

#### GET /api/v1/pieces

Query: ?status=PRACTICING&sort=RECENT_PRACTICE&page=0&size=20

Response (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "체르니 30번 - 8번 연습곡",
      "composer": "Carl Czerny",
      "difficulty": 3,
      "status": "PRACTICING",
      "progressPercent": 65,
      "totalPracticeSeconds": 45000,
      "lastPracticedAt": "2026-03-19T19:15:20+09:00"
    }
  ],
  "meta": { "total": 8, "page": 0, "size": 20 }
}
```

### 4.6 목표 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/v1/goals | 목표 조회 |
| PUT | /api/v1/goals/daily | 일일 목표 설정 |
| PUT | /api/v1/goals/weekly | 주간 목표 설정 |
| POST | /api/v1/goals/piece | 곡 완성 목표 추가 |
| GET | /api/v1/goals/streak | 스트릭 조회 |

#### GET /api/v1/goals

Response (200):
```json
{
  "success": true,
  "data": {
    "daily": { "targetMinutes": 60, "achievedMinutes": 45, "percent": 75 },
    "weekly": { "targetDays": 5, "achievedDays": 4, "targetMinutes": 300, "achievedMinutes": 270 },
    "streak": { "currentDays": 12, "longestDays": 21 },
    "pieceGoals": [
      {
        "id": 1,
        "piece": { "id": 1, "title": "체르니 30번 - 8번" },
        "targetDate": "2026-04-15",
        "currentProgressPercent": 65,
        "daysRemaining": 27
      }
    ]
  }
}
```

### 4.7 타임라인 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/v1/timeline | 타임라인 이벤트 목록 |

### 4.8 대시보드 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/v1/dashboard | 홈 대시보드 데이터 |

Response (200):
```json
{
  "success": true,
  "data": {
    "today": {
      "goalMinutes": 60, "achievedMinutes": 45, "percent": 75,
      "sessions": []
    },
    "streak": { "currentDays": 12, "weeklyAchievedDays": 4, "weeklyTargetDays": 5 },
    "latestLessonNote": {},
    "activePieces": [],
    "weeklyChart": []
  }
}
```

### 4.9 사용자 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/v1/users/me | 프로필 조회 |
| PATCH | /api/v1/users/me | 프로필 수정 |

### 4.10 이미지 업로드 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/uploads/presigned-url | Presigned URL 발급 |

### 4.11 API 엔드포인트 요약 (MVP 범위)

| 메서드 | 경로 | MVP |
|--------|------|-----|
| POST | /api/v1/auth/signup | O |
| POST | /api/v1/auth/login | O |
| POST | /api/v1/auth/social | O |
| POST | /api/v1/auth/refresh | O |
| POST | /api/v1/auth/logout | O |
| POST | /api/v1/practice-sessions | O |
| PATCH | /api/v1/practice-sessions/{id}/switch-piece | O |
| PATCH | /api/v1/practice-sessions/{id}/end | O |
| GET | /api/v1/practice-sessions | O |
| GET | /api/v1/practice-sessions/{id} | O |
| GET | /api/v1/practice-sessions/stats/weekly | O |
| GET | /api/v1/practice-sessions/stats/monthly | Phase 2 |
| POST | /api/v1/lesson-notes | O |
| GET | /api/v1/lesson-notes | O |
| GET | /api/v1/lesson-notes/{id} | O |
| PUT | /api/v1/lesson-notes/{id} | O |
| DELETE | /api/v1/lesson-notes/{id} | O |
| PATCH | /api/v1/lesson-notes/{nId}/assignments/{aId} | Phase 2 |
| POST | /api/v1/pieces | O |
| GET | /api/v1/pieces | O |
| GET | /api/v1/pieces/{id} | O |
| PUT | /api/v1/pieces/{id} | O |
| DELETE | /api/v1/pieces/{id} | O |
| POST | /api/v1/pieces/{id}/section-memos | Phase 3 |
| GET | /api/v1/goals | O |
| PUT | /api/v1/goals/daily | O |
| PUT | /api/v1/goals/weekly | Phase 2 |
| POST | /api/v1/goals/piece | Phase 3 |
| GET | /api/v1/goals/streak | Phase 2 |
| GET | /api/v1/timeline | Phase 3 |
| GET | /api/v1/users/me | O |
| PATCH | /api/v1/users/me | O |
| POST | /api/v1/uploads/presigned-url | Phase 3 |
| GET | /api/v1/dashboard | O |

---

## 5. 프로젝트 구조

### 5.1 백엔드 (Kotlin + Spring Boot)

도메인별 패키지 구조 (feature-based):

```
piano-api/
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yml
├── Dockerfile
└── src/
    ├── main/
    │   ├── kotlin/com/pianocompanion/api/
    │   │   ├── PianoApiApplication.kt
    │   │   ├── domain/
    │   │   │   ├── auth/
    │   │   │   │   ├── controller/AuthController.kt
    │   │   │   │   ├── service/AuthService.kt
    │   │   │   │   ├── service/JwtTokenProvider.kt
    │   │   │   │   ├── service/SocialLoginService.kt
    │   │   │   │   └── dto/
    │   │   │   ├── user/
    │   │   │   │   ├── controller/UserController.kt
    │   │   │   │   ├── service/UserService.kt
    │   │   │   │   ├── repository/UserRepository.kt
    │   │   │   │   ├── entity/User.kt
    │   │   │   │   └── dto/
    │   │   │   ├── practice/
    │   │   │   │   ├── controller/PracticeSessionController.kt
    │   │   │   │   ├── service/PracticeSessionService.kt
    │   │   │   │   ├── service/PracticeStatsService.kt
    │   │   │   │   ├── repository/PracticeSessionRepository.kt
    │   │   │   │   ├── repository/PracticeSessionPieceRepository.kt
    │   │   │   │   ├── entity/PracticeSession.kt
    │   │   │   │   ├── entity/PracticeSessionPiece.kt
    │   │   │   │   └── dto/
    │   │   │   ├── lesson/
    │   │   │   │   ├── controller/LessonNoteController.kt
    │   │   │   │   ├── service/LessonNoteService.kt
    │   │   │   │   ├── repository/
    │   │   │   │   ├── entity/
    │   │   │   │   └── dto/
    │   │   │   ├── piece/
    │   │   │   │   ├── controller/PieceController.kt
    │   │   │   │   ├── service/PieceService.kt
    │   │   │   │   ├── repository/
    │   │   │   │   ├── entity/
    │   │   │   │   └── dto/
    │   │   │   ├── goal/
    │   │   │   │   ├── controller/GoalController.kt
    │   │   │   │   ├── service/GoalService.kt
    │   │   │   │   ├── service/StreakService.kt
    │   │   │   │   ├── repository/
    │   │   │   │   ├── entity/
    │   │   │   │   └── dto/
    │   │   │   ├── timeline/
    │   │   │   │   ├── controller/TimelineController.kt
    │   │   │   │   ├── service/TimelineService.kt
    │   │   │   │   ├── repository/
    │   │   │   │   ├── entity/
    │   │   │   │   └── dto/
    │   │   │   └── dashboard/
    │   │   │       ├── controller/DashboardController.kt
    │   │   │       ├── service/DashboardService.kt
    │   │   │       └── dto/
    │   │   ├── global/
    │   │   │   ├── config/
    │   │   │   ├── security/
    │   │   │   ├── exception/
    │   │   │   ├── common/
    │   │   │   └── util/
    │   │   └── infra/
    │   │       ├── s3/S3Service.kt
    │   │       └── redis/RedisCacheService.kt
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    └── test/kotlin/com/pianocompanion/api/
        ├── domain/
        └── support/IntegrationTestBase.kt
```

### 5.2 프론트엔드 (Next.js 15)

```
piano-web/
├── package.json
├── next.config.ts
├── tailwind.config.ts
└── src/
    ├── app/
    │   ├── layout.tsx
    │   ├── page.tsx
    │   ├── (auth)/
    │   │   ├── login/page.tsx
    │   │   ├── signup/page.tsx
    │   │   └── onboarding/page.tsx
    │   └── (main)/
    │       ├── layout.tsx
    │       ├── home/page.tsx
    │       ├── practice/
    │       │   ├── page.tsx
    │       │   ├── timer/page.tsx
    │       │   ├── summary/page.tsx
    │       │   └── [id]/page.tsx
    │       ├── lessons/
    │       │   ├── page.tsx
    │       │   ├── new/page.tsx
    │       │   └── [id]/page.tsx
    │       ├── pieces/
    │       │   ├── page.tsx
    │       │   ├── new/page.tsx
    │       │   └── [id]/page.tsx
    │       └── stats/page.tsx
    ├── components/
    │   ├── ui/
    │   ├── layout/
    │   ├── practice/
    │   ├── lesson/
    │   ├── piece/
    │   ├── goal/
    │   ├── stats/
    │   └── common/
    ├── hooks/
    ├── stores/
    ├── api/
    ├── types/
    ├── lib/
    └── styles/globals.css
```

---

## 6. 인증/인가 방식

### 6.1 JWT 기반 인증 플로우

```
클라이언트                    Spring Boot               Redis
   │                              │                       │
   │ 1. POST /auth/login          │                       │
   │─────────────────────────────→│                       │
   │                              │ 2. 비밀번호 검증       │
   │                              │ 3. Access Token 생성   │
   │                              │    (만료: 30분)        │
   │                              │ 4. Refresh Token 생성  │
   │                              │    (만료: 14일)        │
   │                              │ 5. RT 저장             │
   │                              │───────────────────────→│
   │ 6. { accessToken, RT }       │                       │
   │←─────────────────────────────│                       │
   │                              │                       │
   │ 7. API 요청 (Bearer AT)      │                       │
   │─────────────────────────────→│                       │
   │                              │ 8. JWT 검증            │
   │ 9. 응답                      │                       │
   │←─────────────────────────────│                       │
   │                              │                       │
   │ 10. AT 만료 → POST /refresh  │                       │
   │─────────────────────────────→│                       │
   │                              │ 11. RT 검증            │
   │                              │───────────────────────→│
   │                              │ 12. 유효 확인           │
   │                              │←───────────────────────│
   │                              │ 13. 새 AT+RT 발급      │
   │                              │ 14. 이전 RT 무효화     │
   │                              │───────────────────────→│
   │ 15. 새 토큰 반환              │                       │
   │←─────────────────────────────│                       │
```

### 6.2 Token 전략

| 항목 | Access Token | Refresh Token |
|------|-------------|---------------|
| 용도 | API 인증 | Access Token 재발급 |
| 만료 시간 | 30분 | 14일 |
| 저장 위치 (클라이언트) | 메모리 (Zustand) | HttpOnly Secure Cookie |
| 저장 위치 (서버) | 저장 안 함 (Stateless) | Redis |
| 갱신 방식 | Refresh Token으로 재발급 | Rotation (재발급 시 갱신) |

### 6.3 Refresh Token Rotation

1. 클라이언트가 Refresh Token으로 갱신 요청
2. 서버가 Redis에서 해당 RT 유효성 확인
3. 유효하면: 이전 RT 삭제, 새 AT + RT 발급, 새 RT를 Redis에 저장
4. 무효하면(재사용 감지): 해당 사용자의 모든 RT 무효화 (보안 위협으로 판단)

Redis 저장 구조:
```
Key: refresh_token:{userId}:{tokenId}
Value: { token, userAgent, createdAt, expiresAt }
TTL: 14 days
```

### 6.4 소셜 로그인 (Google, Apple)

| Provider | ID Token 검증 방법 | 필요 정보 |
|----------|--------------------|----------|
| Google | Google OAuth2 API로 ID Token 검증 | email, name, profile image |
| Apple | Apple Public Key로 JWT 검증 | email (최초 1회만), name |

플로우:
1. 클라이언트에서 Google/Apple OAuth 동의 화면 실행
2. ID Token 획득 후 POST /api/v1/auth/social로 전송
3. 서버에서 ID Token 검증
4. 사용자 Upsert (신규면 생성, 기존이면 조회)
5. JWT 토큰 발급 및 반환
6. isNewUser=true이면 클라이언트에서 온보딩 화면으로 이동

### 6.5 보안 설정

- CORS: 프론트엔드 도메인만 허용
- CSRF: Stateless이므로 비활성화
- Session: STATELESS
- 인증 제외 경로: /api/v1/auth/**, /actuator/health
- 모든 /api/v1/** 경로: 인증 필요
- Rate Limiting: Bucket4j + Redis (인증 API: 10req/min, 일반 API: 100req/min)

---

## 7. MVP 개발 우선순위

### 7.1 개발 단계 및 의존 관계

**Phase 0: 프로젝트 세팅**
- 백엔드 프로젝트 초기화 (Spring Initializr)
- 프론트엔드 프로젝트 초기화 (create-next-app)
- Docker Compose (PostgreSQL, Redis)
- CI/CD 파이프라인 (GitHub Actions)
- DB 마이그레이션 세팅 (Flyway)

**Phase 1: 인증 & 사용자** (의존: Phase 0)
- DB: users 테이블
- BE: 회원가입/로그인/토큰갱신/로그아웃
- BE: Google 소셜 로그인
- BE: JWT 필터, SecurityConfig
- FE: 로그인/회원가입 페이지
- FE: 인증 상태 관리 (Zustand)
- FE: 온보딩 플로우

**Phase 2: 곡 관리** (의존: Phase 1)
- DB: pieces 테이블
- BE: 곡 CRUD API
- FE: 곡 목록/등록/상세 페이지
- FE: 곡 상태 변경 UI

**Phase 3: 연습 기록** (의존: Phase 2)
- DB: practice_sessions, practice_session_pieces 테이블
- BE: 연습 세션 시작/곡전환/종료 API
- BE: 연습 기록 목록 API, 주간 통계 API
- FE: 연습 타이머 (Web Worker 기반)
- FE: 연습 완료 요약/히스토리 페이지
- FE: 곡 선택 바텀 시트

**Phase 4: 레슨 노트** (의존: Phase 2, 병렬 가능 with Phase 3)
- DB: lesson_notes, lesson_note_pieces, lesson_assignments 테이블
- BE: 레슨 노트 CRUD API
- FE: 레슨 노트 작성/목록/상세 페이지
- FE: 과제 체크리스트 UI

**Phase 5: 목표 & 대시보드** (의존: Phase 3)
- DB: goals 테이블
- BE: 목표 설정/조회 API, 대시보드 집계 API, 스트릭 계산 로직
- FE: 홈 대시보드 페이지
- FE: 목표 프로그레스 링, 주간 미니 차트

### 7.2 의존 관계 다이어그램

```
Phase 0 (프로젝트 세팅)
    │
    ▼
Phase 1 (인증 & 사용자)
    │
    ▼
Phase 2 (곡 관리)
    │
    ├──────────────────┐
    ▼                  ▼
Phase 3 (연습 기록)   Phase 4 (레슨 노트)  ← 병렬 개발 가능
    │                  │
    └────────┬─────────┘
             ▼
Phase 5 (목표 & 대시보드)
```

### 7.3 MVP 이후 로드맵

**Phase 2 기능 (Should Have):**
- 월별 연습 캘린더 (히트맵), 곡별 연습 시간 집계
- 과제 관리, 레슨-곡 태그 연결
- 난이도/진행도 상세, 곡 목록 필터/정렬
- 주간 목표, 연속 스트릭

**Phase 3 기능 (Could Have):**
- 이미지 첨부 (S3 Presigned URL)
- 구간별 메모, 타임라인 뷰/마일스톤
- 곡 완성 목표, 자동 타임라인 이벤트 생성

**향후 확장:**
- React Native / Capacitor로 네이티브 앱 전환
- Apple 소셜 로그인
- 데이터 내보내기 (CSV/PDF)
- 오프라인 지원 (PWA + Service Worker)
- 교사-학생 연동 기능

---

## 부록: 주요 아키텍처 결정 기록 (ADR)

### ADR-001: 웹 앱 우선 전략
- **컨텍스트**: 빠른 MVP 출시와 피드백 수집 필요
- **결정**: Next.js 웹 앱으로 MVP, 이후 PWA/네이티브 전환
- **근거**: 단일 코드베이스로 빠른 개발, 앱 스토어 심사 없이 즉시 배포
- **상태**: 승인

### ADR-002: 모놀리식 아키텍처
- **컨텍스트**: MVP 단계에서 최소 복잡도 필요
- **결정**: 단일 Spring Boot 애플리케이션, 도메인별 패키지 분리
- **근거**: 배포/운영 단순화, 향후 패키지 경계를 따라 분리 가능
- **상태**: 승인

### ADR-003: Refresh Token Rotation
- **컨텍스트**: 토큰 탈취 시 보안 대응 필요
- **결정**: RT 1회 사용 후 교체, 재사용 감지 시 전체 무효화
- **근거**: XSS 토큰 탈취 시 피해 범위 최소화
- **상태**: 승인

### ADR-004: 소프트 삭제 (Soft Delete)
- **컨텍스트**: PRD 6.5 - 계정 삭제 시 30일 보존 정책
- **결정**: users, pieces, lesson_notes에 deleted_at 컬럼
- **근거**: 데이터 복구 가능, 인덱스 WHERE deleted_at IS NULL로 성능 유지
- **상태**: 승인

### ADR-005: BFF 패턴 (Next.js API Routes)
- **컨텍스트**: 프론트엔드에서 직접 백엔드 호출 시 CORS, 토큰 관리 복잡성
- **결정**: Next.js API Routes를 BFF로 활용
- **근거**: HttpOnly Cookie로 RT 안전 관리, CORS 이슈 제거, 서버 컴포넌트에서 직접 API 호출 가능
- **상태**: 승인
