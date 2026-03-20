package com.pianocompanion.api.domain.practice.repository

import com.pianocompanion.api.domain.practice.entity.PracticeSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PracticeSessionRepository : JpaRepository<PracticeSession, Long> {

    fun findByIdAndUserId(id: Long, userId: Long): PracticeSession?

    fun findByUserIdOrderByStartedAtDesc(userId: Long, pageable: Pageable): Page<PracticeSession>

    @Query(
        """
        SELECT ps FROM PracticeSession ps
        WHERE ps.userId = :userId
          AND ps.startedAt >= :startDate
          AND ps.startedAt < :endDate
          AND ps.endedAt IS NOT NULL
        ORDER BY ps.startedAt ASC
        """,
    )
    fun findCompletedSessionsInRange(
        userId: Long,
        startDate: Instant,
        endDate: Instant,
    ): List<PracticeSession>

    @Query(
        """
        SELECT COALESCE(SUM(ps.totalDurationSeconds), 0)
        FROM PracticeSession ps
        WHERE ps.userId = :userId
          AND ps.startedAt >= :startDate
          AND ps.startedAt < :endDate
          AND ps.endedAt IS NOT NULL
        """,
    )
    fun sumDurationInRange(userId: Long, startDate: Instant, endDate: Instant): Int

    @Query(
        """
        SELECT COUNT(DISTINCT CAST(ps.startedAt AS date))
        FROM PracticeSession ps
        WHERE ps.userId = :userId
          AND ps.startedAt >= :startDate
          AND ps.startedAt < :endDate
          AND ps.endedAt IS NOT NULL
        """,
    )
    fun countDistinctPracticeDaysInRange(userId: Long, startDate: Instant, endDate: Instant): Int
}
