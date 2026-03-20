# Task #3: 홈/대시보드 + 연습 페이지 디자인 개선

**작업자**: ui-designer
**작업일**: 2026-03-21
**상태**: 완료

---

## 변경 파일 및 내용

### home/page.tsx
- 모든 하드코딩 색상을 semantic token으로 교체:
  - `text-[#212121]` → `text-foreground`
  - `text-[#616161]` → `text-muted-foreground`
  - `text-[#3F51B5]` → `text-primary`
  - `bg-[#3F51B5]` → Button default variant
  - `text-[#FF6D00]` → `text-[--streak]`
  - `bg-[#E8EAF6] text-[#1A237E]` → `bg-accent text-accent-foreground`
- 차트 Cell: `fill-primary` / `fill-border`로 교체

### practice/page.tsx
- 하드코딩 색상 제거
- 빈 상태일 때 EmptyState 컴포넌트 활용 추가
- Button에서 불필요한 `bg-[#3F51B5]` 제거 (default variant 사용)

### practice/timer/page.tsx
- 모든 하드코딩 색상 → semantic token
- Button에서 불필요한 색상 클래스 제거

### practice/summary/page.tsx
- 하드코딩 색상 → semantic token
- `hover:bg-gray-50` → `hover:bg-muted`
- `bg-[#E8EAF6]` → `bg-accent`

## 해결된 감사 이슈
- [C1] 하드코딩 색상 교체 (홈/연습 페이지 범위)
- [H2] EmptyState 적용 (연습 히스토리)
