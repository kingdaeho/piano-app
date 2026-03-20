package com.pianocompanion.api.domain.dashboard.service

import com.pianocompanion.api.domain.dashboard.dto.DashboardStreakView
import com.pianocompanion.api.domain.dashboard.dto.DashboardView
import com.pianocompanion.api.domain.dashboard.dto.TodayView
import com.pianocompanion.api.domain.goal.service.GoalService
import com.pianocompanion.api.domain.lesson.dto.LessonNoteView
import com.pianocompanion.api.domain.lesson.repository.LessonNoteRepository
import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.dto.DailyStatView
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Service
class DashboardService(
    private val practiceSessionRepository: PracticeSessionRepository,
    private val pieceRepository: PieceRepository,
    private val lessonNoteRepository: LessonNoteRepository,
    private val goalService: GoalService,
) {

    @Transactional(readOnly = true)
    fun getDashboard(userId: Long, zoneId: ZoneId): DashboardView {
        val goals = goalService.getGoals(userId, zoneId)

        val today = LocalDate.now(zoneId)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(DAYS_IN_WEEK.toLong())
        val weekStartInstant = weekStart.atStartOfDay(zoneId).toInstant()
        val weekEndInstant = weekEnd.atStartOfDay(zoneId).toInstant()
        val sessions = practiceSessionRepository.findCompletedSessionsInRange(
            userId,
            weekStartInstant,
            weekEndInstant,
        )
        val dailyMap = sessions.groupBy { it.startedAt.atZone(zoneId).toLocalDate() }
        val weeklyChart = (0 until DAYS_IN_WEEK).map { dayOffset ->
            val day = weekStart.plusDays(dayOffset.toLong())
            val dayDuration = dailyMap[day]?.sumOf { it.totalDurationSeconds } ?: 0
            DailyStatView(date = day, durationSeconds = dayDuration)
        }

        val activePieces = pieceRepository.findActivePiecesByUserId(userId)
            .map { PieceSummaryView.from(it) }

        val latestNote = lessonNoteRepository.findLatestByUserId(userId)
        val latestLessonNoteView = latestNote?.let { note ->
            val pieceIds = note.notePieces.map { it.pieceId }
            val pieces = if (pieceIds.isNotEmpty()) {
                pieceRepository.findByIdInAndUserIdAndDeletedAtIsNull(pieceIds, userId)
            } else {
                emptyList()
            }
            LessonNoteView.from(note, pieces)
        }

        return DashboardView(
            today = TodayView(
                goalMinutes = goals.daily.targetMinutes,
                achievedMinutes = goals.daily.achievedMinutes,
                percent = goals.daily.percent,
            ),
            streak = DashboardStreakView(
                currentDays = goals.streak.currentDays,
                weeklyAchievedDays = goals.weekly.achievedDays,
                weeklyTargetDays = goals.weekly.targetDays,
            ),
            latestLessonNote = latestLessonNoteView,
            activePieces = activePieces,
            weeklyChart = weeklyChart,
        )
    }

    companion object {
        private const val DAYS_IN_WEEK = 7
    }
}
