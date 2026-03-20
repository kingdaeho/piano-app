package com.pianocompanion.api.domain.practice.dto

import com.pianocompanion.api.domain.practice.entity.Mood
import com.pianocompanion.api.domain.practice.entity.PracticeSession
import com.pianocompanion.api.domain.practice.entity.PracticeSessionPiece
import java.time.Instant
import java.time.LocalDate

data class StartSessionRequest(
    val pieceId: Long? = null,
)

data class SwitchPieceRequest(
    val pieceId: Long,
)

data class EndSessionRequest(
    val memo: String? = null,
    val mood: Mood? = null,
)

data class PracticeSessionView(
    val id: Long,
    val startedAt: Instant,
    val endedAt: Instant?,
    val totalDurationSeconds: Int,
    val memo: String?,
    val mood: Mood?,
    val pieces: List<SessionPieceView>,
) {
    companion object {
        fun from(session: PracticeSession, pieceNames: Map<Long, String> = emptyMap()): PracticeSessionView =
            PracticeSessionView(
                id = session.id,
                startedAt = session.startedAt,
                endedAt = session.endedAt,
                totalDurationSeconds = session.totalDurationSeconds,
                memo = session.memo,
                mood = session.mood,
                pieces = session.pieces.map { SessionPieceView.from(it, pieceNames) },
            )
    }
}

data class SessionPieceView(
    val pieceId: Long,
    val title: String,
    val durationSeconds: Int,
    val orderIndex: Int,
) {
    companion object {
        fun from(sp: PracticeSessionPiece, pieceNames: Map<Long, String> = emptyMap()): SessionPieceView =
            SessionPieceView(
                pieceId = sp.pieceId,
                title = pieceNames[sp.pieceId] ?: "",
                durationSeconds = sp.durationSeconds,
                orderIndex = sp.orderIndex,
            )
    }
}

data class WeeklyStatsView(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val totalDurationSeconds: Int,
    val practiceDays: Int,
    val dailyStats: List<DailyStatView>,
    val pieceStats: List<PieceStatView>,
    val previousWeekDurationSeconds: Int,
    val changePercent: Double,
)

data class DailyStatView(
    val date: LocalDate,
    val durationSeconds: Int,
)

data class PieceStatView(
    val pieceId: Long,
    val title: String,
    val durationSeconds: Int,
    val percent: Double,
)
