# Piano App 배포 가이드

## 배포 순서

DB -> Redis -> Backend -> Frontend 순서로 진행합니다. 의존성이 있는 서비스부터 먼저 프로비저닝합니다.

---

## 1. Supabase (PostgreSQL) 프로젝트 생성

1. [supabase.com](https://supabase.com) 접속 후 로그인
2. "New Project" 클릭
3. 프로젝트 설정:
   - Name: `piano-companion`
   - Database Password: 강력한 비밀번호 생성 (아래 명령어 참고)
   - Region: `Northeast Asia (Tokyo)` 권장
4. 프로젝트 생성 후 Settings > Database에서 연결 정보 확인:
   - Host, Port, Database name, User, Password
5. Connection string 형식:
   ```
   jdbc:postgresql://<host>:5432/postgres?user=postgres&password=<password>
   ```

### 주의사항
- "Use connection pooling" 옵션을 활성화하면 포트가 6543으로 변경됩니다
- Flyway 마이그레이션은 direct connection (포트 5432)으로 실행해야 합니다
- Connection pooling (포트 6543)은 애플리케이션 런타임용입니다

---

## 2. Upstash Redis 생성

1. [upstash.com](https://upstash.com) 접속 후 로그인
2. "Create Database" 클릭
3. 설정:
   - Name: `piano-companion-redis`
   - Region: `ap-northeast-1` (Tokyo)
   - TLS: 활성화
4. 생성 후 Details 탭에서 확인:
   - `UPSTASH_REDIS_REST_URL`
   - `UPSTASH_REDIS_REST_TOKEN`
   - Spring Boot용 연결 정보: Host, Port, Password

### Spring Boot Redis 설정값
```
SPRING_DATA_REDIS_HOST=<upstash-endpoint>
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=<upstash-password>
SPRING_DATA_REDIS_SSL_ENABLED=true
```

---

## 3. JWT Secret 생성

다음 명령어로 안전한 JWT 시크릿을 생성합니다:

```bash
openssl rand -base64 64
```

이 값을 `JWT_SECRET` 환경 변수로 사용합니다. 절대 코드에 하드코딩하지 마세요.

---

## 4. Railway 백엔드 배포

### 프로젝트 생성
1. [railway.app](https://railway.app) 접속 후 로그인
2. "New Project" > "Deploy from GitHub repo" 선택
3. piano-app 저장소 연결
4. Root Directory를 `backend`로 설정
5. Railway가 `Dockerfile`을 자동 감지합니다

### 환경 변수 설정

Railway 대시보드 > Variables 탭에서 다음을 설정합니다:

```
# Database (Supabase)
SPRING_DATASOURCE_URL=jdbc:postgresql://<supabase-host>:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<supabase-db-password>

# Redis (Upstash)
SPRING_DATA_REDIS_HOST=<upstash-endpoint>
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=<upstash-password>
SPRING_DATA_REDIS_SSL_ENABLED=true

# JWT
JWT_SECRET=<openssl로 생성한 값>

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Flyway
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true

# Server
PORT=8080
```

### 배포 확인
- Railway가 자동으로 빌드 및 배포합니다
- `/actuator/health` 엔드포인트로 헬스체크가 실행됩니다
- Logs 탭에서 애플리케이션 로그를 확인할 수 있습니다

---

## 5. Vercel 프론트엔드 배포

### 프로젝트 생성
1. [vercel.com](https://vercel.com) 접속 후 로그인
2. "Add New Project" > GitHub 저장소 연결
3. Framework Preset: `Next.js` (자동 감지)
4. Root Directory: `frontend`

### 환경 변수 설정

Vercel 대시보드 > Settings > Environment Variables:

```
NEXT_PUBLIC_API_URL=https://<railway-backend-url>
NEXT_PUBLIC_APP_ENV=production
```

### 도메인 설정 (선택)
1. Settings > Domains에서 커스텀 도메인 추가
2. DNS에 CNAME 레코드 추가: `cname.vercel-dns.com`

---

## 6. 환경 변수 설정 순서 요약

설정 순서가 중요합니다. 의존 관계가 있는 값부터 먼저 생성합니다:

1. Supabase DB 비밀번호 생성 -> `SPRING_DATASOURCE_*` 값 확보
2. Upstash Redis 생성 -> `SPRING_DATA_REDIS_*` 값 확보
3. JWT Secret 생성 -> `JWT_SECRET` 값 확보
4. Railway에 위 모든 값을 환경 변수로 설정
5. Railway 배포 완료 후 Backend URL 확보
6. Vercel에 `NEXT_PUBLIC_API_URL` 설정 (Railway URL 사용)

---

## 7. 배포 후 검증

### Backend 검증
```bash
# 헬스체크
curl https://<railway-url>/actuator/health

# API 문서 (Swagger UI)
# https://<railway-url>/swagger-ui.html
```

### Frontend 검증
```bash
# 페이지 접근
curl -I https://<vercel-url>
```

### Flyway 마이그레이션 확인
- 첫 배포 시 Flyway가 자동으로 마이그레이션을 실행합니다
- `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true`로 기존 DB에도 안전하게 적용됩니다

---

## 롤백 절차

### Backend 롤백
1. Railway 대시보드 > Deployments 탭
2. 이전 성공 배포 선택 > "Redeploy" 클릭

### Frontend 롤백
1. Vercel 대시보드 > Deployments 탭
2. 이전 성공 배포 선택 > "Promote to Production" 클릭

### DB 마이그레이션 롤백
- Flyway는 기본적으로 롤백을 지원하지 않습니다
- 파괴적 변경(컬럼 삭제, 테이블 삭제) 전에 반드시 백업을 수행하세요:
  ```bash
  pg_dump -h <host> -U postgres -d postgres > backup_$(date +%Y%m%d_%H%M%S).sql
  ```
