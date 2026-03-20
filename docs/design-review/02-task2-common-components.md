# Task #2: 공통 컴포넌트 디자인 개선

**작업자**: ui-designer
**작업일**: 2026-03-21
**상태**: 완료

---

## 변경 파일 및 내용

### globals.css
- 새 디자인 토큰 추가: `--success`, `--warning`, `--streak`, `--primary-hover`, `--nav-bg`, `--nav-inactive`, `--nav-inactive-hover`
- 다크모드 대응값 추가

### bottom-nav.tsx
- 하드코딩된 색상 제거: `#3F51B5`, `#9E9E9E`, `bg-white/95`
- CSS 변수로 교체: `text-primary`, `--nav-inactive`, `--nav-bg`
- 다크모드 완전 호환

### top-bar.tsx
- `bg-white/95` → `--nav-bg`
- `hover:bg-gray-100` → Button ghost variant 사용
- 다크모드 호환

### empty-state.tsx
- 하드코딩 색상 → semantic tokens: `text-foreground`, `text-muted-foreground`, `bg-muted`
- 아이콘을 원형 배경 안에 배치하여 시각적 완성도 향상

### progress-ring.tsx
- 하드코딩된 SVG stroke(`#E8EAF6`, `#3F51B5`) → Tailwind class(`stroke-accent`, `stroke-primary`)
- 다크모드 자동 대응

### (main)/layout.tsx
- `bg-[#FAFAFA]` → `bg-background`

### (auth)/layout.tsx
- `bg-[#E8EAF6]` → `bg-accent`

## 해결된 감사 이슈
- [C2] 메인 레이아웃 배경색 하드코딩
- [C3] BottomNav 다크모드 미지원
- [H1] TopBar 컴포넌트 스타일 개선 (다크모드 호환)
- [H2] EmptyState 컴포넌트 스타일 개선
