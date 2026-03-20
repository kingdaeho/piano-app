package com.pianocompanion.api.domain.goal

import com.pianocompanion.api.domain.goal.dto.SetDailyGoalRequest
import com.pianocompanion.api.domain.goal.dto.SetWeeklyGoalRequest
import com.pianocompanion.api.domain.goal.entity.Goal
import com.pianocompanion.api.domain.goal.entity.GoalType
import com.pianocompanion.api.domain.goal.repository.GoalRepository
import com.pianocompanion.api.domain.goal.service.GoalService
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.domain.user.entity.AuthProvider
import com.pianocompanion.api.domain.user.entity.User
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.global.exception.EntityNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.ZoneId
import java.util.Optional

class GoalServiceTest : BehaviorSpec({
    val goalRepository = mockk<GoalRepository>()
    val userRepository = mockk<UserRepository>()
    val practiceSessionRepository = mockk<PracticeSessionRepository>()
    val pieceRepository = mockk<PieceRepository>()

    val goalService = GoalService(goalRepository, userRepository, practiceSessionRepository, pieceRepository)
    val userId = 1L
    val zoneId = ZoneId.of("Asia/Seoul")

    val user = User(
        email = "test@example.com",
        name = "테스트",
        provider = AuthProvider.LOCAL,
        dailyGoalMinutes = 60,
        weeklyGoalDays = 5,
        weeklyGoalMinutes = 300,
    )

    given("목표 조회") {
        `when`("사용자가 존재하고 연습 기록이 없는 경우") {
            every { userRepository.findById(userId) } returns Optional.of(user)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 0
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 0
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns emptyList()

            val result = goalService.getGoals(userId, zoneId)

            then("기본 목표와 0% 달성률을 반환한다") {
                result.daily.targetMinutes shouldBe 60
                result.daily.achievedMinutes shouldBe 0
                result.daily.percent shouldBe 0
                result.weekly.targetDays shouldBe 5
                result.weekly.achievedDays shouldBe 0
                result.streak.currentDays shouldBe 0
                result.pieceGoals shouldBe emptyList()
            }
        }

        `when`("오늘 30분 연습한 경우") {
            every { userRepository.findById(userId) } returns Optional.of(user)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 1800
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 1
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns emptyList()

            val result = goalService.getGoals(userId, zoneId)

            then("50% 달성률을 반환한다") {
                result.daily.achievedMinutes shouldBe 30
                result.daily.percent shouldBe 50
            }
        }

        `when`("사용자가 존재하지 않는 경우") {
            every { userRepository.findById(99L) } returns Optional.empty()

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    goalService.getGoals(99L, zoneId)
                }
            }
        }
    }

    given("일일 목표 설정") {
        `when`("유효한 목표를 설정하면") {
            every { userRepository.findById(userId) } returns Optional.of(user)

            goalService.setDailyGoal(userId, SetDailyGoalRequest(targetMinutes = 90))

            then("사용자의 일일 목표가 변경된다") {
                user.dailyGoalMinutes shouldBe 90
            }
        }

        `when`("존재하지 않는 사용자의 목표를 설정하면") {
            every { userRepository.findById(99L) } returns Optional.empty()

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    goalService.setDailyGoal(99L, SetDailyGoalRequest(targetMinutes = 60))
                }
            }
        }
    }

    given("주간 목표 설정") {
        `when`("일수와 시간을 모두 설정하면") {
            val freshUser = User(email = "u@e.com", name = "u", provider = AuthProvider.LOCAL)
            every { userRepository.findById(2L) } returns Optional.of(freshUser)

            goalService.setWeeklyGoal(2L, SetWeeklyGoalRequest(targetDays = 6, targetMinutes = 420))

            then("사용자의 주간 목표가 변경된다") {
                freshUser.weeklyGoalDays shouldBe 6
                freshUser.weeklyGoalMinutes shouldBe 420
            }
        }

        `when`("일수만 설정하면") {
            val freshUser = User(
                email = "u2@e.com",
                name = "u2",
                provider = AuthProvider.LOCAL,
                weeklyGoalMinutes = 300,
            )
            every { userRepository.findById(3L) } returns Optional.of(freshUser)

            goalService.setWeeklyGoal(3L, SetWeeklyGoalRequest(targetDays = 4))

            then("일수만 변경되고 시간은 유지된다") {
                freshUser.weeklyGoalDays shouldBe 4
                freshUser.weeklyGoalMinutes shouldBe 300
            }
        }

        `when`("존재하지 않는 사용자의 주간 목표를 설정하면") {
            every { userRepository.findById(99L) } returns Optional.empty()

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    goalService.setWeeklyGoal(99L, SetWeeklyGoalRequest(targetDays = 3))
                }
            }
        }
    }

    given("목표 달성률 계산 - 엣지 케이스") {
        `when`("목표를 100% 초과하여 달성하면") {
            val userWith30MinGoal = User(
                email = "over@e.com",
                name = "over",
                provider = AuthProvider.LOCAL,
                dailyGoalMinutes = 30,
            )
            every { userRepository.findById(userId) } returns Optional.of(userWith30MinGoal)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 3600
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 1
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns emptyList()

            val result = goalService.getGoals(userId, zoneId)

            then("달성률이 100%로 제한된다") {
                result.daily.percent shouldBe 100
            }
        }

        `when`("일일 목표가 0분인 경우") {
            val zeroGoalUser = User(
                email = "zero@e.com",
                name = "zero",
                provider = AuthProvider.LOCAL,
                dailyGoalMinutes = 0,
            )
            every { userRepository.findById(userId) } returns Optional.of(zeroGoalUser)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 1800
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 1
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns emptyList()

            val result = goalService.getGoals(userId, zoneId)

            then("달성률이 0%이다") {
                result.daily.percent shouldBe 0
            }
        }
    }

    given("곡별 목표 조회 - 엣지 케이스") {
        `when`("목표에 연결된 곡이 삭제된 경우") {
            every { userRepository.findById(userId) } returns Optional.of(user)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 0
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 0

            val goal = Goal(
                userId = userId,
                pieceId = 999L,
                type = GoalType.PIECE_COMPLETION,
                targetValue = 100,
            )
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns listOf(goal)
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(999L, userId) } returns null

            val result = goalService.getGoals(userId, zoneId)

            then("해당 목표가 결과에서 제외된다") {
                result.pieceGoals shouldBe emptyList()
            }
        }

        `when`("목표에 pieceId가 null인 경우") {
            every { userRepository.findById(userId) } returns Optional.of(user)
            every { practiceSessionRepository.sumDurationInRange(eq(userId), any(), any()) } returns 0
            every { practiceSessionRepository.countDistinctPracticeDaysInRange(eq(userId), any(), any()) } returns 0

            val goal = Goal(
                userId = userId,
                pieceId = null,
                type = GoalType.PIECE_COMPLETION,
                targetValue = 100,
            )
            every {
                goalRepository.findByUserIdAndTypeInAndIsActiveTrue(userId, listOf(GoalType.PIECE_COMPLETION))
            } returns listOf(goal)

            val result = goalService.getGoals(userId, zoneId)

            then("해당 목표가 결과에서 제외된다") {
                result.pieceGoals shouldBe emptyList()
            }
        }
    }
})
