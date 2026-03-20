package com.pianocompanion.api.domain.goal.dto

import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class SetDailyGoalRequest(
    @field:NotNull(message = "목표 시간(분)은 필수입니다")
    @field:Min(1, message = "목표 시간은 1분 이상이어야 합니다")
    val targetMinutes: Int,
)

data class SetWeeklyGoalRequest(
    val targetDays: Int? = null,
    val targetMinutes: Int? = null,
)

data class GoalsView(
    val daily: DailyGoalView,
    val weekly: WeeklyGoalView,
    val streak: StreakView,
    val pieceGoals: List<PieceGoalView>,
)

data class DailyGoalView(
    val targetMinutes: Int,
    val achievedMinutes: Int,
    val percent: Int,
)

data class WeeklyGoalView(
    val targetDays: Int,
    val achievedDays: Int,
    val targetMinutes: Int,
    val achievedMinutes: Int,
)

data class StreakView(
    val currentDays: Int,
    val longestDays: Int,
)

data class PieceGoalView(
    val id: Long,
    val piece: PieceSummaryView,
    val targetDate: LocalDate?,
    val currentProgressPercent: Int,
    val daysRemaining: Long?,
)
