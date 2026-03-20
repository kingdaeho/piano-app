package com.pianocompanion.api.domain.practice.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "practice_session_pieces")
class PracticeSessionPiece(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: PracticeSession,

    @Column(name = "piece_id", nullable = false)
    val pieceId: Long,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int = 0,
) : BaseEntity()
