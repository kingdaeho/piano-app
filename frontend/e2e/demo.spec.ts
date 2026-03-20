import { test, expect } from "@playwright/test";

test.describe("Piano Practice Companion 데모", () => {
  test("전체 앱 데모 워크스루", async ({ page }) => {
    test.setTimeout(120000);

    // 1. 회원가입 페이지
    await page.goto("/signup");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);
    await page.screenshot({ path: "e2e/results/01-signup.png" });

    // 이름, 이메일, 비밀번호 입력
    await page.fill('input[id="name"]', "김민지");
    await page.fill('input[id="email"]', "minji@example.com");
    await page.fill('input[id="password"]', "password123");
    await page.waitForTimeout(500);
    await page.screenshot({ path: "e2e/results/02-signup-filled.png" });

    // 가입하기 클릭
    await page.click('button[type="submit"]');
    await page.waitForURL("**/home");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(2000);
    await page.screenshot({ path: "e2e/results/03-home-dashboard.png" });

    // 2. 홈(대시보드) - 스크롤하며 전체 확인
    await page.evaluate(() => window.scrollTo({ top: 400, behavior: "smooth" }));
    await page.waitForTimeout(1000);
    await page.screenshot({ path: "e2e/results/04-home-active-pieces.png" });

    await page.evaluate(() => window.scrollTo({ top: 800, behavior: "smooth" }));
    await page.waitForTimeout(1000);
    await page.screenshot({ path: "e2e/results/05-home-weekly-chart.png" });

    // 3. 연습 기록 탭
    await page.goto("/practice");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/06-practice-history.png" });

    // 4. 연습 타이머 시작
    await page.goto("/practice/timer");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/07-practice-piece-select.png" });

    // 곡 선택 시도 - PieceSelector에서 곡 클릭
    const pieceItems = page.locator("button, [class*='cursor-pointer']").filter({ hasText: /체르니|바흐|쇼팽|소나타/ });
    const count = await pieceItems.count();
    if (count > 0) {
      await pieceItems.first().click({ force: true });
      await page.waitForTimeout(3000);
      await page.screenshot({ path: "e2e/results/08-practice-timer-running.png" });

      // 타이머 5초 대기
      await page.waitForTimeout(5000);
      await page.screenshot({ path: "e2e/results/09-practice-timer-5sec.png" });

      // 종료 버튼 클릭
      const stopBtn = page.locator("button", { hasText: "종료" });
      if (await stopBtn.isVisible({ timeout: 3000 })) {
        await stopBtn.click({ force: true });
        await page.waitForTimeout(1500);
        await page.screenshot({ path: "e2e/results/10-practice-summary.png" });
      }
    } else {
      await page.screenshot({ path: "e2e/results/08-practice-timer-no-pieces.png" });
    }

    // 5. 레슨 노트 목록
    await page.goto("/lessons");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/11-lessons-list.png" });

    // 레슨 노트 상세 보기
    await page.goto("/lessons/1");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/12-lesson-detail.png" });

    await page.evaluate(() => window.scrollTo({ top: 500, behavior: "smooth" }));
    await page.waitForTimeout(800);
    await page.screenshot({ path: "e2e/results/13-lesson-detail-scroll.png" });

    // 레슨 노트 작성
    await page.goto("/lessons/new");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);
    await page.screenshot({ path: "e2e/results/14-lesson-new.png" });

    // 6. 곡 관리 목록
    await page.goto("/pieces");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/15-pieces-list.png" });

    // 곡 상세
    await page.goto("/pieces/1");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/16-piece-detail.png" });

    // 곡 등록
    await page.goto("/pieces/new");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    await page.fill('input[placeholder*="체르니"]', "쇼팽 - 녹턴 Op.9 No.2");
    await page.fill('input[placeholder*="Carl"]', "Frédéric Chopin");
    await page.fill('input[placeholder*="클래식"]', "클래식, 낭만파");
    await page.waitForTimeout(500);
    await page.screenshot({ path: "e2e/results/17-piece-new-filled.png" });

    await page.evaluate(() => window.scrollTo({ top: 400, behavior: "smooth" }));
    await page.waitForTimeout(800);
    await page.screenshot({ path: "e2e/results/18-piece-new-options.png" });

    // 7. 통계 - 주간
    await page.goto("/stats");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(2000);
    await page.screenshot({ path: "e2e/results/19-stats-weekly.png" });

    await page.evaluate(() => window.scrollTo({ top: 400, behavior: "smooth" }));
    await page.waitForTimeout(800);
    await page.screenshot({ path: "e2e/results/20-stats-weekly-pieces.png" });

    // 통계 - 목표 탭
    await page.evaluate(() => window.scrollTo({ top: 0, behavior: "smooth" }));
    await page.waitForTimeout(300);
    const goalsTab = page.locator("button", { hasText: "목표" });
    await goalsTab.click({ force: true });
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/21-stats-goals.png" });

    await page.evaluate(() => window.scrollTo({ top: 500, behavior: "smooth" }));
    await page.waitForTimeout(800);
    await page.screenshot({ path: "e2e/results/22-stats-goals-streak.png" });

    // 8. 목표 설정
    await page.goto("/goals");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);
    await page.screenshot({ path: "e2e/results/23-goal-settings.png" });

    // 목표 변경
    const btn90 = page.locator("button", { hasText: "90분" });
    if (await btn90.isVisible({ timeout: 2000 })) {
      await btn90.click({ force: true });
      await page.waitForTimeout(300);
    }
    const btn6days = page.locator("button", { hasText: "6일" });
    if (await btn6days.isVisible({ timeout: 2000 })) {
      await btn6days.click({ force: true });
      await page.waitForTimeout(300);
    }
    await page.screenshot({ path: "e2e/results/24-goal-settings-changed.png" });

    // 최종: 홈으로 돌아가기
    await page.goto("/home");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    await page.screenshot({ path: "e2e/results/25-final-home.png" });
  });
});
