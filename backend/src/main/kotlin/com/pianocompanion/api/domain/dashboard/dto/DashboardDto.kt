package com.pianocompanion.api.domain.dashboard.dto

import com.pianocompanion.api.domain.goal.dto.DailyGoalView
import com.pianocompanion.api.domain.goal.dto.StreakView
import com.pianocompanion.api.domain.lesson.dto.LessonNoteView
import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import com.pianocompanion.api.domain.practice.dto.DailyStatView

data class DashboardView(
    val today: TodayView,
    val streak: DashboardStreakView,
    val latestLessonNote: LessonNoteView?,
    val activePieces: List<PieceSummaryView>,
    val weeklyChart: List<DailyStatView>,
)

data class TodayView(
    val goalMinutes: Int,
    val achievedMinutes: Int,
    val percent: Int,
)

data class DashboardStreakView(
    val currentDays: Int,
    val weeklyAchievedDays: Int,
    val weeklyTargetDays: Int,
)
