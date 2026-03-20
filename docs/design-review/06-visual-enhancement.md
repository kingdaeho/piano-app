# 시각적 품질 향상 (병렬 작업)

**작업자**: designer-a, designer-b
**작업일**: 2026-03-21
**상태**: 완료

---

## designer-a: 홈/연습/공통 컴포넌트

### home/page.tsx
- 오늘의 목표 카드에 그라데이션 배경 추가 (`bg-gradient-to-br from-card via-card to-accent/30`)
- 섹션 제목들에 `tracking-tight` 추가로 타이포그래피 강화
- 곡 카드에 Music 아이콘 추가

### practice/timer/page.tsx
- 타이머 숫자 크기: `text-5xl` → `text-6xl`
- 시작/정지 버튼: `h-16 w-16` → `h-18 w-18`, 아이콘 `h-9 w-9`

### practice/summary/page.tsx
- 축하 요소: 녹색 원형 배경 + CheckCircle2 아이콘 + "수고하셨습니다!" 텍스트
- 곡별 연습 시간 카드에 Music 아이콘
- 목표 달성률 카드에 Target 아이콘

### bottom-nav.tsx
- 활성 탭에 상단 인디케이터 바 추가 (`h-0.5 w-5 rounded-full bg-primary`)

### empty-state.tsx
- 아이콘 배경: `h-20 w-20 rounded-2xl` → `h-24 w-24 rounded-full`

---

## designer-b: 레슨/곡관리/통계/인증

### lessons/page.tsx
- 카드에 `shadow-sm` + `hover:shadow-md` 트랜지션
- 회차 번호를 Badge 컴포넌트로 변경
- 날짜를 `text-xs`로 축소

### pieces/page.tsx
- 카드에 Music2 아이콘 박스 추가
- 난이도 뱃지 컬러 차별화: <=2 green, =3 amber, >=4 red
- 상태 뱃지: 연습중=primary, 완성=green, 나머지=기본
- 카드 hover shadow 효과

### stats/page.tsx
- 주간 네비게이션을 Button variant="ghost" size="icon" className="h-11 w-11"로 래핑 (44px 터치 타겟)
- 통계 카드에 아이콘 추가: Clock, Calendar, TrendingUp, Target, Flame
- 달성률 100% 이상시 success 색상

### goals/page.tsx
- (designer-b 범위에 포함되었으나 별도 변경 불필요로 판단)

### login/page.tsx, signup/page.tsx
- 이모지 🎹 → Music lucide 아이콘 (primary 배경)
- 앱 이름 "Piano Companion" 통일
- 카드 shadow 강화, 폼 간격 통일 (space-y-5)

### piece-selector.tsx
- 곡 항목에 난이도 뱃지 추가 (별 + 컬러 차별화)

---

## 해결된 감사 이슈

| 이슈 | 상태 |
|------|------|
| M1. 카드 그림자/깊이감 부족 | 해결 (shadow 강화, 그라데이션 추가) |
| M2. 타이포그래피 계층 약함 | 해결 (tracking-tight, 크기 조절) |
| H3. 터치 타겟 미달 | 해결 (Button 래핑, 버튼 크기 강화) |
| M6. 연습 완료 축하 요소 부재 | 해결 (CheckCircle2 + 텍스트) |
| M7. 곡 카드 아이콘 부재 | 해결 (Music/Music2 아이콘 추가) |
| H4. 하단 네비 활성 상태 | 해결 (인디케이터 바 추가) |
