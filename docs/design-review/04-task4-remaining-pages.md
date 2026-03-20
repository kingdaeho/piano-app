# Task #4: 레슨/곡관리/통계/목표/인증 페이지 디자인 개선

**작업자**: ui-designer
**작업일**: 2026-03-21
**상태**: 완료

---

## 변경 파일 및 내용

### lessons/page.tsx
- 하드코딩 색상 제거, EmptyState 컴포넌트 활용
- Button에서 bg-[#3F51B5] 제거

### lessons/new/page.tsx
- 하드코딩 색상 → semantic token
- plain button → Button ghost variant
- text-[#F44336] → text-destructive

### lessons/[id]/page.tsx
- 하드코딩 색상 → semantic token
- plain button → Button ghost variant

### pieces/page.tsx
- 하드코딩 색상 제거, EmptyState 활용
- text-[#4CAF50] → text-success

### pieces/new/page.tsx
- 하드코딩 색상 → semantic token
- hover:bg-gray-50 → hover:bg-muted
- border-[#E0E0E0] → border-border

### pieces/[id]/page.tsx
- 하드코딩 색상 → semantic token

### stats/page.tsx
- 하드코딩 색상 제거
- plain button → Button ghost
- 차트 fill → semantic class
- text-[#4CAF50]/text-[#F44336] → text-success/text-destructive
- text-[#FF6D00] → text-[--streak]

### goals/page.tsx
- 하드코딩 색상 제거
- plain button → Button ghost variant

### login/page.tsx
- text-[#1A237E] → text-accent-foreground
- text-[#616161] → text-muted-foreground
- bg-[#3F51B5] → Button default

### signup/page.tsx
- 동일 패턴 적용

### piece-selector.tsx
- 하드코딩 색상 → semantic token
- hover:bg-gray-50 → hover:bg-muted

### auth-guard.tsx
- border-[#3F51B5] → border-primary

### star-rating.tsx
- fill-[#FFC107] → fill-secondary
- text-[#E0E0E0] → text-border

## 전체 결과
- tsx 파일에 하드코딩된 hex 색상: **0개**
- TypeScript 컴파일 에러: **0개**
- 모든 컴포넌트가 CSS 변수 기반 디자인 토큰 사용
- 다크모드 완전 호환

## 해결된 감사 이슈
- [C1] 하드코딩 색상 교체 (전체 완료)
- [H2] EmptyState 적용 (레슨, 곡 목록)
