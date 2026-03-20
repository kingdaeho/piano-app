package com.pianocompanion.api.domain.piece.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "pieces")
class Piece(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(length = 100)
    var composer: String? = null,

    @Column(length = 50)
    var genre: String? = null,

    @Column
    var difficulty: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PieceStatus = PieceStatus.NOT_STARTED,

    @Column(name = "progress_percent", nullable = false)
    var progressPercent: Int = 0,

    @Column(columnDefinition = "TEXT")
    var memo: String? = null,

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) : BaseEntity() {

    fun update(
        title: String? = null,
        composer: String? = null,
        genre: String? = null,
        difficulty: Int? = null,
        status: PieceStatus? = null,
        progressPercent: Int? = null,
        memo: String? = null,
    ) {
        title?.let { this.title = it }
        composer?.let { this.composer = it }
        genre?.let { this.genre = it }
        difficulty?.let { this.difficulty = it }
        status?.let { updateStatus(it) }
        progressPercent?.let { this.progressPercent = it }
        memo?.let { this.memo = it }
    }

    private fun updateStatus(newStatus: PieceStatus) {
        if (this.status != newStatus) {
            if (newStatus == PieceStatus.PRACTICING && this.startedAt == null) {
                this.startedAt = Instant.now()
            }
            if (newStatus == PieceStatus.COMPLETED && this.completedAt == null) {
                this.completedAt = Instant.now()
                this.progressPercent = MAX_PROGRESS
            }
            this.status = newStatus
        }
    }

    fun softDelete() {
        this.deletedAt = Instant.now()
    }

    companion object {
        const val MAX_PROGRESS = 100
    }
}

enum class PieceStatus {
    NOT_STARTED,
    PRACTICING,
    FINISHING,
    COMPLETED,
    ON_HOLD,
}
