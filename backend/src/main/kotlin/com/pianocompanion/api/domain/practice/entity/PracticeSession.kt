package com.pianocompanion.api.domain.practice.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "practice_sessions")
class PracticeSession(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,

    @Column(name = "ended_at")
    var endedAt: Instant? = null,

    @Column(name = "total_duration_seconds", nullable = false)
    var totalDurationSeconds: Int = 0,

    @Column(columnDefinition = "TEXT")
    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var mood: Mood? = null,

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    val pieces: MutableList<PracticeSessionPiece> = mutableListOf(),
) : BaseEntity() {

    fun end(memo: String?, mood: Mood?) {
        val now = Instant.now()
        this.endedAt = now
        this.totalDurationSeconds = java.time.Duration.between(startedAt, now).seconds.toInt()
        this.memo = memo
        this.mood = mood
    }

    fun addPiece(pieceId: Long): PracticeSessionPiece {
        val orderIndex = pieces.size
        val sessionPiece = PracticeSessionPiece(
            session = this,
            pieceId = pieceId,
            orderIndex = orderIndex,
        )
        pieces.add(sessionPiece)
        return sessionPiece
    }

    fun switchPiece(newPieceId: Long): PracticeSessionPiece {
        val now = Instant.now()
        val currentPiece = pieces.lastOrNull()
        if (currentPiece != null && currentPiece.durationSeconds == 0) {
            val elapsed = java.time.Duration.between(currentPiece.createdAt, now).seconds.toInt()
            currentPiece.durationSeconds = elapsed
        }
        return addPiece(newPieceId)
    }
}

enum class Mood {
    GREAT,
    GOOD,
    OK,
    BAD,
    TERRIBLE,
}
