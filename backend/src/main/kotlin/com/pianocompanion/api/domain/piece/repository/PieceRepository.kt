package com.pianocompanion.api.domain.piece.repository

import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PieceRepository : JpaRepository<Piece, Long> {

    fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): Piece?

    @Query(
        """
        SELECT p FROM Piece p
        WHERE p.userId = :userId
          AND p.deletedAt IS NULL
          AND (:status IS NULL OR p.status = :status)
        ORDER BY p.updatedAt DESC
        """,
    )
    fun findByUserIdAndStatus(userId: Long, status: PieceStatus?, pageable: Pageable): Page<Piece>

    fun findAllByUserIdAndDeletedAtIsNull(userId: Long): List<Piece>

    @Query(
        """
        SELECT p FROM Piece p
        WHERE p.userId = :userId
          AND p.deletedAt IS NULL
          AND p.status IN ('PRACTICING', 'FINISHING')
        ORDER BY p.updatedAt DESC
        """,
    )
    fun findActivePiecesByUserId(userId: Long): List<Piece>

    fun countByUserIdAndStatusAndDeletedAtIsNull(userId: Long, status: PieceStatus): Long
}
