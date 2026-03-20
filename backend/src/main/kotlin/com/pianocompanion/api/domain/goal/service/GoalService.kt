package com.pianocompanion.api.domain.goal.service

import com.pianocompanion.api.domain.goal.dto.DailyGoalView
import com.pianocompanion.api.domain.goal.dto.GoalsView
import com.pianocompanion.api.domain.goal.dto.PieceGoalView
import com.pianocompanion.api.domain.goal.dto.SetDailyGoalRequest
import com.pianocompanion.api.domain.goal.dto.SetWeeklyGoalRequest
import com.pianocompanion.api.domain.goal.dto.StreakView
import com.pianocompanion.api.domain.goal.dto.WeeklyGoalView
import com.pianocompanion.api.domain.goal.entity.Goal
import com.pianocompanion.api.domain.goal.entity.GoalType
import com.pianocompanion.api.domain.goal.repository.GoalRepository
import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository,
    private val practiceSessionRepository: PracticeSessionRepository,
    private val pieceRepository: PieceRepository,
) {

    private val logger = getLogger()

    @Transactional(readOnly = true)
    fun getGoals(userId: Long, zoneId: ZoneId): GoalsView {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }

        val today = LocalDate.now(zoneId)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(DAYS_IN_WEEK.toLong())

        val todayStart = today.atStartOfDay(zoneId).toInstant()
        val todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant()
        val weekStartInstant = weekStart.atStartOfDay(zoneId).toInstant()
        val weekEndInstant = weekEnd.atStartOfDay(zoneId).toInstant()

        val todayDuration = practiceSessionRepository.sumDurationInRange(userId, todayStart, todayEnd)
        val todayMinutes = todayDuration / SECONDS_PER_MINUTE

        val weekDuration = practiceSessionRepository.sumDurationInRange(userId, weekStartInstant, weekEndInstant)
        val weekMinutes = weekDuration / SECONDS_PER_MINUTE
        val weekDays = practiceSessionRepository.countDistinctPracticeDaysInRange(
            userId,
            weekStartInstant,
            weekEndInstant,
        )

        val dailyGoal = DailyGoalView(
            targetMinutes = user.dailyGoalMinutes,
            achievedMinutes = todayMinutes,
            percent = calculatePercent(todayMinutes, user.dailyGoalMinutes),
        )

        val weeklyGoal = WeeklyGoalView(
            targetDays = user.weeklyGoalDays,
            achievedDays = weekDays,
            targetMinutes = user.weeklyGoalMinutes,
            achievedMinutes = weekMinutes,
        )

        val streak = calculateStreak(userId, today, zoneId, user.dailyGoalMinutes)

        val pieceGoals = goalRepository.findByUserIdAndTypeInAndIsActiveTrue(
            userId,
            listOf(GoalType.PIECE_COMPLETION),
        ).mapNotNull { goal ->
            val pieceId = goal.pieceId ?: return@mapNotNull null
            val piece = pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(pieceId, userId) ?: return@mapNotNull null
            PieceGoalView(
                id = goal.id,
                piece = PieceSummaryView.from(piece),
                targetDate = goal.targetDate,
                currentProgressPercent = piece.progressPercent,
                daysRemaining = goal.targetDate?.let { ChronoUnit.DAYS.between(today, it) },
            )
        }

        return GoalsView(
            daily = dailyGoal,
            weekly = weeklyGoal,
            streak = streak,
            pieceGoals = pieceGoals,
        )
    }

    @Transactional
    fun setDailyGoal(userId: Long, request: SetDailyGoalRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }
        user.dailyGoalMinutes = request.targetMinutes
        logger.info("Daily goal updated: userId={}, targetMinutes={}", userId, request.targetMinutes)
    }

    @Transactional
    fun setWeeklyGoal(userId: Long, request: SetWeeklyGoalRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }
        request.targetDays?.let { user.weeklyGoalDays = it }
        request.targetMinutes?.let { user.weeklyGoalMinutes = it }
        logger.info("Weekly goal updated: userId={}", userId)
    }

    private fun calculateStreak(userId: Long, today: LocalDate, zoneId: ZoneId, dailyGoalMinutes: Int): StreakView {
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var checkDate = today

        for (i in 0 until MAX_STREAK_CHECK_DAYS) {
            val dayStart = checkDate.atStartOfDay(zoneId).toInstant()
            val dayEnd = checkDate.plusDays(1).atStartOfDay(zoneId).toInstant()
            val dayDuration = practiceSessionRepository.sumDurationInRange(userId, dayStart, dayEnd)
            val dayMinutes = dayDuration / SECONDS_PER_MINUTE

            if (dayMinutes >= dailyGoalMinutes) {
                tempStreak++
                if (i == 0 || currentStreak > 0) {
                    currentStreak = tempStreak
                }
            } else {
                if (i == 0) {
                    currentStreak = 0
                }
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 0
            }
            checkDate = checkDate.minusDays(1)
        }
        longestStreak = maxOf(longestStreak, tempStreak, currentStreak)

        return StreakView(currentDays = currentStreak, longestDays = longestStreak)
    }

    private fun calculatePercent(achieved: Int, target: Int): Int {
        if (target <= 0) return 0
        return minOf((achieved * PERCENT_MAX / target), PERCENT_MAX)
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        private const val PERCENT_MAX = 100
        private const val DAYS_IN_WEEK = 7
        private const val MAX_STREAK_CHECK_DAYS = 365
    }
}
