# Piano Practice Companion - 서비스 런칭 보고서

**작성일**: 2026-03-21
**상태**: 런칭 완료

---

## 1. 프로젝트 개요

피아노 연습 기록, 레슨 노트, 통계 관리를 위한 웹 애플리케이션.

| 구성 | 기술 스택 |
|------|-----------|
| Frontend | Next.js 16, React 19, shadcn/ui, TanStack Query, Zustand |
| Backend | Kotlin + Spring Boot 3.4.3, Java 21, Spring Security + JWT |
| Database | PostgreSQL (Flyway 마이그레이션 6개) |
| Cache | Redis (세션/토큰 관리) |

---

## 2. 배포 아키텍처

```
[사용자 브라우저]
    │
    ├─► Vercel (Frontend)
    │     https://frontend-snowy-gamma-50.vercel.app
    │
    └─► Render (Backend)
          https://piano-app-qzoa.onrender.com
              │
              ├─► Supabase PostgreSQL (Tokyo, Session Pooler)
              └─► Upstash Redis (Tokyo, TLS)
```

### 최종 서비스 구성

| 구성 요소 | 서비스 | 플랜 | 리전 | 월 비용 |
|-----------|--------|------|------|---------|
| Frontend | Vercel | Hobby (무료) | Auto | $0 |
| Backend | Render | Starter | Singapore | $7 |
| Database | Supabase | Free (500MB) | Tokyo | $0 |
| Cache | Upstash Redis | Free (10K 명령/일) | Tokyo | $0 |
| 소스 관리 | GitHub | Free | - | $0 |
| **합계** | | | | **$7/월** |

---

## 3. 진행 과정

### Phase 1: 인프라 설계

**서비스 비교 및 선정 과정:**

- Frontend 호스팅: Vercel vs Cloudflare Pages vs Netlify → **Vercel** 선정 (Next.js 공식 지원)
- Backend 호스팅: Railway vs Cloud Run vs Render vs Fly.io → 최초 **Railway** 선정
- Database: Supabase vs Railway Postgres vs Neon → **Supabase** 선정 (무료 500MB, 연결 풀링 내장)
- Cache: Upstash Redis vs Railway Redis → **Upstash** 선정 (서버리스, 무료 티어)

### Phase 2: CI/CD 파이프라인 + 배포 준비

**생성된 파일:**

| 파일 | 용도 |
|------|------|
| `.github/workflows/backend-ci.yml` | Backend 테스트/빌드 CI |
| `.github/workflows/backend-deploy.yml` | 배포 후 헬스체크 |
| `.github/workflows/frontend-ci.yml` | Frontend lint/build 검증 |
| `backend/Dockerfile` | 멀티스테이지 빌드 (JDK→JRE, non-root, JVM 최적화) |
| `backend/.dockerignore` | Docker 빌드 컨텍스트 최적화 |
| `frontend/vercel.json` | Vercel 설정 + 보안 헤더 |
| `frontend/.env.example` | 환경 변수 예시 |
| `docs/DEPLOYMENT.md` | 프로비저닝 가이드 |

### Phase 3: 클라우드 프로비저닝 및 배포

**순서:**

1. **GitHub CLI 설치** → `gh auth login` → `kingdaeho/piano-app` 리포 생성 + 푸시
2. **Supabase 프로젝트 생성** → `piano-companion` (Tokyo, CLI로 생성)
3. **Upstash Redis 생성** → `piano-companion-redis` (Tokyo, 웹 대시보드)
4. **Backend 배포 시도 (Railway)** → CLI 불안정, Dockerfile 인식 실패로 **Render로 전환**
5. **Render 배포** → Docker 빌드 성공, 환경 변수 설정
6. **Vercel 프론트엔드 배포** → `vercel --prod`

### Phase 4: 트러블슈팅

| 문제 | 원인 | 해결 |
|------|------|------|
| Railway 배포 실패 | CLI가 Dockerfile을 인식하지 못함, Root Directory 설정 불가 | **Render로 전환** |
| DB 연결 실패 (Network unreachable) | Supabase DB가 IPv6 전용, Render가 IPv6 미지원 | **Supabase Session Pooler (IPv4)** 사용 |
| DB 연결 실패 (Tenant not found) | Pooler 사용 시 username 형식이 `user.project-ref` 필요 | `postgres.eaewbcbljsepudrqkysk`로 변경 |
| Flyway 마이그레이션 실패 | Transaction mode pooler와 Flyway 비호환 | **Session mode pooler (5432)** 로 전환 |
| API 경로 불일치 | `NEXT_PUBLIC_API_URL`에 `/api/v1` 누락 | URL에 `/api/v1` 경로 포함 |
| CORS 에러 | `CORS_ALLOWED_ORIGINS`가 실제 Vercel URL과 불일치 | 실제 URL로 수정 |

---

## 4. 환경 변수 목록

### Backend (Render)

| 변수명 | 용도 |
|--------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | Supabase Session Pooler JDBC URL |
| `DATABASE_USERNAME` | `postgres.eaewbcbljsepudrqkysk` |
| `DATABASE_PASSWORD` | Supabase DB 비밀번호 |
| `REDIS_HOST` | Upstash 엔드포인트 |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | Upstash 비밀번호 |
| `SPRING_DATA_REDIS_SSL_ENABLED` | `true` |
| `JWT_SECRET` | JWT 서명 키 (base64, 88자) |
| `JWT_ACCESS_EXPIRY` | `3600000` (1시간) |
| `JWT_REFRESH_EXPIRY` | `604800000` (7일) |
| `CORS_ALLOWED_ORIGINS` | Vercel 프론트엔드 URL |

### Frontend (Vercel)

| 변수명 | 용도 |
|--------|------|
| `NEXT_PUBLIC_API_URL` | `https://piano-app-qzoa.onrender.com/api/v1` |
| `NEXT_PUBLIC_APP_ENV` | `production` |

---

## 5. API 엔드포인트 (28개)

| 도메인 | 엔드포인트 수 | 주요 기능 |
|--------|-------------|-----------|
| 인증 | 4 | 회원가입, 로그인, 토큰 갱신, 로그아웃 |
| 사용자 | 2 | 프로필 조회/수정 |
| 대시보드 | 1 | 대시보드 데이터 |
| 곡 관리 | 5 | CRUD |
| 연습 기록 | 6 | 시작, 곡 전환, 종료, 목록, 상세, 주간 통계 |
| 레슨 노트 | 7 | CRUD + 과제 토글 |
| 목표 | 3 | 조회, 일일/주간 목표 설정 |

---

## 6. 배포 파이프라인

```
git push origin main
    │
    ├─► GitHub Actions
    │     ├─► backend-ci: test → build (backend/** 변경 시)
    │     └─► frontend-ci: lint → build (frontend/** 변경 시)
    │
    ├─► Render (자동)
    │     backend/ 변경 감지 → Docker 빌드 → 배포 → 헬스체크
    │
    └─► Vercel (자동)
          frontend/ 변경 감지 → Next.js 빌드 → 배포
```

---

## 7. 서비스 URL

| 용도 | URL |
|------|-----|
| 프론트엔드 | https://frontend-snowy-gamma-50.vercel.app |
| 백엔드 API | https://piano-app-qzoa.onrender.com/api/v1 |
| 헬스체크 | https://piano-app-qzoa.onrender.com/actuator/health |
| API 문서 (Swagger) | https://piano-app-qzoa.onrender.com/swagger-ui.html |
| GitHub 리포 | https://github.com/kingdaeho/piano-app |
| Supabase 대시보드 | https://supabase.com/dashboard/project/eaewbcbljsepudrqkysk |
| Upstash 대시보드 | https://console.upstash.com |
| Render 대시보드 | https://dashboard.render.com |

---

## 8. 향후 개선 사항

- [ ] 커스텀 도메인 연결 (Vercel + Render)
- [ ] Sentry 오류 모니터링 추가
- [ ] Render 자동 스케일링 (사용자 증가 시)
- [ ] Supabase Pro 업그레이드 (백업, Point-in-Time Recovery)
- [ ] GitHub Actions에 E2E 테스트 추가
