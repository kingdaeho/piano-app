package com.pianocompanion.api.domain.practice.repository

import com.pianocompanion.api.domain.practice.entity.PracticeSessionPiece
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PracticeSessionPieceRepository : JpaRepository<PracticeSessionPiece, Long> {

    @Query(
        """
        SELECT psp.pieceId, COALESCE(SUM(psp.durationSeconds), 0)
        FROM PracticeSessionPiece psp
        JOIN psp.session ps
        WHERE ps.userId = :userId
          AND ps.startedAt >= :startDate
          AND ps.startedAt < :endDate
          AND ps.endedAt IS NOT NULL
        GROUP BY psp.pieceId
        """,
    )
    fun sumDurationByPieceInRange(
        userId: Long,
        startDate: Instant,
        endDate: Instant,
    ): List<Array<Any>>
}
