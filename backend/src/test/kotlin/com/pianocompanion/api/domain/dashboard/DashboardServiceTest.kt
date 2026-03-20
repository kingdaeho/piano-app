package com.pianocompanion.api.domain.dashboard

import com.pianocompanion.api.domain.dashboard.service.DashboardService
import com.pianocompanion.api.domain.goal.dto.DailyGoalView
import com.pianocompanion.api.domain.goal.dto.GoalsView
import com.pianocompanion.api.domain.goal.dto.StreakView
import com.pianocompanion.api.domain.goal.dto.WeeklyGoalView
import com.pianocompanion.api.domain.goal.service.GoalService
import com.pianocompanion.api.domain.lesson.repository.LessonNoteRepository
import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.global.exception.EntityNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.ZoneId

class DashboardServiceTest : BehaviorSpec({
    val practiceSessionRepository = mockk<PracticeSessionRepository>()
    val pieceRepository = mockk<PieceRepository>()
    val lessonNoteRepository = mockk<LessonNoteRepository>()
    val goalService = mockk<GoalService>()

    val dashboardService = DashboardService(
        practiceSessionRepository,
        pieceRepository,
        lessonNoteRepository,
        goalService,
    )
    val userId = 1L
    val zoneId = ZoneId.of("Asia/Seoul")

    given("대시보드 조회") {
        `when`("사용자가 존재하고 데이터가 없는 경우") {
            every { goalService.getGoals(userId, zoneId) } returns GoalsView(
                daily = DailyGoalView(targetMinutes = 60, achievedMinutes = 0, percent = 0),
                weekly = WeeklyGoalView(targetDays = 5, achievedDays = 0, targetMinutes = 300, achievedMinutes = 0),
                streak = StreakView(currentDays = 0, longestDays = 0),
                pieceGoals = emptyList(),
            )
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findActivePiecesByUserId(userId) } returns emptyList()
            every { lessonNoteRepository.findLatestByUserId(userId) } returns null

            val result = dashboardService.getDashboard(userId, zoneId)

            then("빈 대시보드를 반환한다") {
                result.today.goalMinutes shouldBe 60
                result.today.achievedMinutes shouldBe 0
                result.today.percent shouldBe 0
                result.streak.currentDays shouldBe 0
                result.activePieces shouldBe emptyList()
                result.latestLessonNote shouldBe null
                result.weeklyChart.size shouldBe 7
            }
        }

        `when`("활성 곡이 있는 경우") {
            every { goalService.getGoals(userId, zoneId) } returns GoalsView(
                daily = DailyGoalView(targetMinutes = 60, achievedMinutes = 30, percent = 50),
                weekly = WeeklyGoalView(targetDays = 5, achievedDays = 1, targetMinutes = 300, achievedMinutes = 30),
                streak = StreakView(currentDays = 1, longestDays = 1),
                pieceGoals = emptyList(),
            )
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findActivePiecesByUserId(userId) } returns listOf(
                Piece(userId = userId, title = "테스트곡", status = PieceStatus.PRACTICING, progressPercent = 50),
            )
            every { lessonNoteRepository.findLatestByUserId(userId) } returns null

            val result = dashboardService.getDashboard(userId, zoneId)

            then("활성 곡 목록이 포함된다") {
                result.activePieces.size shouldBe 1
                result.activePieces.first().title shouldBe "테스트곡"
                result.today.achievedMinutes shouldBe 30
                result.today.percent shouldBe 50
            }
        }

        `when`("존재하지 않는 사용자의 대시보드를 조회하면") {
            every { goalService.getGoals(99L, zoneId) } throws EntityNotFoundException("User", 99L)

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    dashboardService.getDashboard(99L, zoneId)
                }
            }
        }

        `when`("일일 목표가 0분인 사용자가 연습한 경우") {
            every { goalService.getGoals(userId, zoneId) } returns GoalsView(
                daily = DailyGoalView(targetMinutes = 0, achievedMinutes = 30, percent = 0),
                weekly = WeeklyGoalView(targetDays = 5, achievedDays = 0, targetMinutes = 300, achievedMinutes = 0),
                streak = StreakView(currentDays = 0, longestDays = 0),
                pieceGoals = emptyList(),
            )
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findActivePiecesByUserId(userId) } returns emptyList()
            every { lessonNoteRepository.findLatestByUserId(userId) } returns null

            val result = dashboardService.getDashboard(userId, zoneId)

            then("달성률이 0%이다") {
                result.today.percent shouldBe 0
            }
        }

        `when`("주간 차트 데이터가 올바르게 7일치 반환되는지") {
            every { goalService.getGoals(userId, zoneId) } returns GoalsView(
                daily = DailyGoalView(targetMinutes = 60, achievedMinutes = 0, percent = 0),
                weekly = WeeklyGoalView(targetDays = 5, achievedDays = 0, targetMinutes = 300, achievedMinutes = 0),
                streak = StreakView(currentDays = 0, longestDays = 0),
                pieceGoals = emptyList(),
            )
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findActivePiecesByUserId(userId) } returns emptyList()
            every { lessonNoteRepository.findLatestByUserId(userId) } returns null

            val result = dashboardService.getDashboard(userId, zoneId)

            then("주간 차트가 정확히 7일이다") {
                result.weeklyChart.size shouldBe 7
                result.weeklyChart.all { it.durationSeconds == 0 } shouldBe true
            }
        }
    }
})
