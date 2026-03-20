package com.pianocompanion.api.domain.practice.service

import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.dto.DailyStatView
import com.pianocompanion.api.domain.practice.dto.PieceStatView
import com.pianocompanion.api.domain.practice.dto.WeeklyStatsView
import com.pianocompanion.api.domain.practice.repository.PracticeSessionPieceRepository
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Service
class PracticeStatsService(
    private val practiceSessionRepository: PracticeSessionRepository,
    private val practiceSessionPieceRepository: PracticeSessionPieceRepository,
    private val pieceRepository: PieceRepository,
) {

    @Transactional(readOnly = true)
    fun getWeeklyStats(userId: Long, date: LocalDate, zoneId: ZoneId): WeeklyStatsView {
        val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(DAYS_IN_WEEK.toLong())

        val startInstant = weekStart.atStartOfDay(zoneId).toInstant()
        val endInstant = weekEnd.atStartOfDay(zoneId).toInstant()

        val sessions = practiceSessionRepository.findCompletedSessionsInRange(userId, startInstant, endInstant)

        val totalDuration = sessions.sumOf { it.totalDurationSeconds }
        val dailyMap = sessions.groupBy { it.startedAt.atZone(zoneId).toLocalDate() }
        val practiceDays = dailyMap.size

        val dailyStats = (0 until DAYS_IN_WEEK).map { dayOffset ->
            val day = weekStart.plusDays(dayOffset.toLong())
            val dayDuration = dailyMap[day]?.sumOf { it.totalDurationSeconds } ?: 0
            DailyStatView(date = day, durationSeconds = dayDuration)
        }

        val pieceNames = pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId)
            .associate { it.id to it.title }
        val pieceDurations = practiceSessionPieceRepository
            .sumDurationByPieceInRange(userId, startInstant, endInstant)
        val pieceStats = pieceDurations.map { row ->
            val pieceId = (row[0] as Number).toLong()
            val duration = (row[1] as Number).toInt()
            PieceStatView(
                pieceId = pieceId,
                title = pieceNames[pieceId] ?: "",
                durationSeconds = duration,
                percent = if (totalDuration > 0) (duration * 100.0 / totalDuration) else 0.0,
            )
        }.sortedByDescending { it.durationSeconds }

        // Previous week
        val prevWeekStart = weekStart.minusWeeks(1)
        val prevStartInstant = prevWeekStart.atStartOfDay(zoneId).toInstant()
        val prevEndInstant = weekStart.atStartOfDay(zoneId).toInstant()
        val prevWeekDuration = practiceSessionRepository.sumDurationInRange(userId, prevStartInstant, prevEndInstant)

        val changePercent = if (prevWeekDuration > 0) {
            ((totalDuration - prevWeekDuration) * 100.0 / prevWeekDuration)
        } else {
            0.0
        }

        return WeeklyStatsView(
            weekStart = weekStart,
            weekEnd = weekStart.plusDays(DAYS_IN_WEEK.toLong() - 1),
            totalDurationSeconds = totalDuration,
            practiceDays = practiceDays,
            dailyStats = dailyStats,
            pieceStats = pieceStats,
            previousWeekDurationSeconds = prevWeekDuration,
            changePercent = changePercent,
        )
    }

    companion object {
        private const val DAYS_IN_WEEK = 7
    }
}
