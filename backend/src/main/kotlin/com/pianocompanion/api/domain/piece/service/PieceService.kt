package com.pianocompanion.api.domain.piece.service

import com.pianocompanion.api.domain.piece.dto.CreatePieceRequest
import com.pianocompanion.api.domain.piece.dto.PieceView
import com.pianocompanion.api.domain.piece.dto.UpdatePieceRequest
import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.global.common.PageMeta
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.ForbiddenException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PieceService(
    private val pieceRepository: PieceRepository,
) {

    private val logger = getLogger()

    @Transactional
    fun create(userId: Long, request: CreatePieceRequest): PieceView {
        val piece = Piece(
            userId = userId,
            title = request.title,
            composer = request.composer,
            genre = request.genre,
            difficulty = request.difficulty,
            status = request.status,
            memo = request.memo,
        )
        val saved = pieceRepository.save(piece)
        logger.info("Piece created: id={}, userId={}, title={}", saved.id, userId, saved.title)
        return PieceView.from(saved)
    }

    @Transactional(readOnly = true)
    fun getList(userId: Long, status: PieceStatus?, pageable: Pageable): Pair<List<PieceView>, PageMeta> {
        val page = pieceRepository.findByUserIdAndStatus(userId, status, pageable)
        val views = page.content.map { PieceView.from(it) }
        val meta = PageMeta(
            total = page.totalElements,
            page = page.number,
            size = page.size,
        )
        return views to meta
    }

    @Transactional(readOnly = true)
    fun getDetail(userId: Long, pieceId: Long): PieceView {
        val piece = findPieceOrThrow(pieceId, userId)
        return PieceView.from(piece)
    }

    @Transactional
    fun update(userId: Long, pieceId: Long, request: UpdatePieceRequest): PieceView {
        val piece = findPieceOrThrow(pieceId, userId)
        piece.update(
            title = request.title,
            composer = request.composer,
            genre = request.genre,
            difficulty = request.difficulty,
            status = request.status,
            progressPercent = request.progressPercent,
            memo = request.memo,
        )
        logger.debug("Piece updated: id={}", pieceId)
        return PieceView.from(piece)
    }

    @Transactional
    fun delete(userId: Long, pieceId: Long) {
        val piece = findPieceOrThrow(pieceId, userId)
        piece.softDelete()
        logger.info("Piece soft deleted: id={}", pieceId)
    }

    private fun findPieceOrThrow(pieceId: Long, userId: Long): Piece {
        val piece = pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(pieceId, userId)
            ?: throw EntityNotFoundException("Piece", pieceId)
        if (piece.userId != userId) {
            throw ForbiddenException("해당 곡에 대한 권한이 없습니다")
        }
        return piece
    }
}
