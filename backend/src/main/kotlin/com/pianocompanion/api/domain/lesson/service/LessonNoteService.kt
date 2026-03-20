package com.pianocompanion.api.domain.lesson.service

import com.pianocompanion.api.domain.lesson.dto.CreateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.dto.LessonNoteView
import com.pianocompanion.api.domain.lesson.dto.UpdateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.entity.LessonAssignment
import com.pianocompanion.api.domain.lesson.entity.LessonNote
import com.pianocompanion.api.domain.lesson.repository.LessonNoteRepository
import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.global.common.PageMeta
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.ForbiddenException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LessonNoteService(
    private val lessonNoteRepository: LessonNoteRepository,
    private val pieceRepository: PieceRepository,
) {

    private val logger = getLogger()

    @Transactional
    fun create(userId: Long, request: CreateLessonNoteRequest): LessonNoteView {
        val lessonNumber = lessonNoteRepository.getNextLessonNumber(userId)

        val note = LessonNote(
            userId = userId,
            lessonNumber = lessonNumber,
            lessonDate = request.lessonDate,
            startTime = request.startTime,
            endTime = request.endTime,
            content = request.content,
            teacherFeedback = request.teacherFeedback,
        )

        request.assignments.forEachIndexed { index, input ->
            note.assignments.add(
                LessonAssignment(
                    lessonNote = note,
                    content = input.content,
                    isCompleted = input.isCompleted,
                    orderIndex = index,
                ),
            )
        }

        note.replacePieceIds(request.pieceIds)

        val saved = lessonNoteRepository.save(note)
        val pieces = loadPieces(request.pieceIds, userId)

        logger.info("Lesson note created: id={}, userId={}, lessonNumber={}", saved.id, userId, lessonNumber)
        return LessonNoteView.from(saved, pieces)
    }

    @Transactional(readOnly = true)
    fun getList(userId: Long, pageable: Pageable): Pair<List<LessonNoteView>, PageMeta> {
        val page = lessonNoteRepository.findByUserIdAndDeletedAtIsNullOrderByLessonDateDesc(userId, pageable)
        val allPieces = pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId).associateBy { it.id }

        val views = page.content.map { note ->
            val pieces = note.notePieces.mapNotNull { allPieces[it.pieceId] }
            LessonNoteView.from(note, pieces)
        }
        val meta = PageMeta(total = page.totalElements, page = page.number, size = page.size)
        return views to meta
    }

    @Transactional(readOnly = true)
    fun getDetail(userId: Long, noteId: Long): LessonNoteView {
        val note = findNoteOrThrow(noteId, userId)
        val pieces = loadPiecesForNote(note, userId)
        return LessonNoteView.from(note, pieces)
    }

    @Transactional
    fun update(userId: Long, noteId: Long, request: UpdateLessonNoteRequest): LessonNoteView {
        val note = findNoteOrThrow(noteId, userId)

        note.update(
            lessonDate = request.lessonDate,
            startTime = request.startTime,
            endTime = request.endTime,
            content = request.content,
            teacherFeedback = request.teacherFeedback,
        )

        request.assignments?.let { inputs ->
            val newAssignments = inputs.mapIndexed { index, input ->
                LessonAssignment(
                    lessonNote = note,
                    content = input.content,
                    isCompleted = input.isCompleted,
                    orderIndex = index,
                )
            }
            note.replaceAssignments(newAssignments)
        }

        request.pieceIds?.let { note.replacePieceIds(it) }

        val pieces = loadPiecesForNote(note, userId)
        logger.debug("Lesson note updated: id={}", noteId)
        return LessonNoteView.from(note, pieces)
    }

    @Transactional
    fun delete(userId: Long, noteId: Long) {
        val note = findNoteOrThrow(noteId, userId)
        note.softDelete()
        logger.info("Lesson note soft deleted: id={}", noteId)
    }

    @Transactional
    fun toggleAssignment(userId: Long, noteId: Long, assignmentId: Long) {
        val note = findNoteOrThrow(noteId, userId)
        val assignment = note.assignments.find { it.id == assignmentId }
            ?: throw EntityNotFoundException("LessonAssignment", assignmentId)
        assignment.toggleCompletion()
        logger.debug("Assignment toggled: id={}, completed={}", assignmentId, assignment.isCompleted)
    }

    private fun findNoteOrThrow(noteId: Long, userId: Long): LessonNote {
        val note = lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(noteId, userId)
            ?: throw EntityNotFoundException("LessonNote", noteId)
        if (note.userId != userId) {
            throw ForbiddenException("해당 레슨 노트에 대한 권한이 없습니다")
        }
        return note
    }

    private fun loadPieces(pieceIds: List<Long>, userId: Long): List<Piece> {
        if (pieceIds.isEmpty()) return emptyList()
        return pieceRepository.findAllById(pieceIds).filter { it.userId == userId && it.deletedAt == null }
    }

    private fun loadPiecesForNote(note: LessonNote, userId: Long): List<Piece> {
        val pieceIds = note.notePieces.map { it.pieceId }
        return loadPieces(pieceIds, userId)
    }
}
