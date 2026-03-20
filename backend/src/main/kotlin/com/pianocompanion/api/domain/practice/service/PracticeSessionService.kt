package com.pianocompanion.api.domain.practice.service

import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.dto.EndSessionRequest
import com.pianocompanion.api.domain.practice.dto.PracticeSessionView
import com.pianocompanion.api.domain.practice.dto.StartSessionRequest
import com.pianocompanion.api.domain.practice.dto.SwitchPieceRequest
import com.pianocompanion.api.domain.practice.entity.PracticeSession
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.global.common.PageMeta
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.ForbiddenException
import com.pianocompanion.api.global.exception.InvalidRequestException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PracticeSessionService(
    private val practiceSessionRepository: PracticeSessionRepository,
    private val pieceRepository: PieceRepository,
) {

    private val logger = getLogger()

    @Transactional
    fun startSession(userId: Long, request: StartSessionRequest): PracticeSessionView {
        val session = PracticeSession(
            userId = userId,
            startedAt = Instant.now(),
        )
        if (request.pieceId != null) {
            validatePieceOwnership(request.pieceId, userId)
            session.addPiece(request.pieceId)
        }
        val saved = practiceSessionRepository.save(session)
        logger.info("Practice session started: id={}, userId={}", saved.id, userId)
        return PracticeSessionView.from(saved, buildPieceNameMap(userId))
    }

    @Transactional
    fun switchPiece(userId: Long, sessionId: Long, request: SwitchPieceRequest): PracticeSessionView {
        val session = findSessionOrThrow(sessionId, userId)
        if (session.endedAt != null) {
            throw InvalidRequestException("이미 종료된 세션입니다")
        }
        validatePieceOwnership(request.pieceId, userId)
        session.switchPiece(request.pieceId)
        logger.debug("Piece switched in session: sessionId={}, pieceId={}", sessionId, request.pieceId)
        return PracticeSessionView.from(session, buildPieceNameMap(userId))
    }

    @Transactional
    fun endSession(userId: Long, sessionId: Long, request: EndSessionRequest): PracticeSessionView {
        val session = findSessionOrThrow(sessionId, userId)
        if (session.endedAt != null) {
            throw InvalidRequestException("이미 종료된 세션입니다")
        }

        // Calculate duration for the last piece
        val lastPiece = session.pieces.lastOrNull()
        if (lastPiece != null && lastPiece.durationSeconds == 0) {
            val elapsed = java.time.Duration.between(lastPiece.createdAt, Instant.now()).seconds.toInt()
            lastPiece.durationSeconds = elapsed
        }

        session.end(request.memo, request.mood)
        logger.info(
            "Practice session ended: id={}, duration={}s",
            sessionId,
            session.totalDurationSeconds,
        )
        return PracticeSessionView.from(session, buildPieceNameMap(userId))
    }

    @Transactional(readOnly = true)
    fun getList(userId: Long, pageable: Pageable): Pair<List<PracticeSessionView>, PageMeta> {
        val page = practiceSessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable)
        val pieceNames = buildPieceNameMap(userId)
        val views = page.content.map { PracticeSessionView.from(it, pieceNames) }
        val meta = PageMeta(total = page.totalElements, page = page.number, size = page.size)
        return views to meta
    }

    @Transactional(readOnly = true)
    fun getDetail(userId: Long, sessionId: Long): PracticeSessionView {
        val session = findSessionOrThrow(sessionId, userId)
        return PracticeSessionView.from(session, buildPieceNameMap(userId))
    }

    private fun findSessionOrThrow(sessionId: Long, userId: Long): PracticeSession {
        val session = practiceSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw EntityNotFoundException("PracticeSession", sessionId)
        if (session.userId != userId) {
            throw ForbiddenException("해당 연습 세션에 대한 권한이 없습니다")
        }
        return session
    }

    private fun validatePieceOwnership(pieceId: Long, userId: Long) {
        val piece = pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(pieceId, userId)
            ?: throw EntityNotFoundException("Piece", pieceId)
        if (piece.userId != userId) {
            throw ForbiddenException("해당 곡에 대한 권한이 없습니다")
        }
    }

    private fun buildPieceNameMap(userId: Long): Map<Long, String> {
        return pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId)
            .associate { it.id to it.title }
    }
}
