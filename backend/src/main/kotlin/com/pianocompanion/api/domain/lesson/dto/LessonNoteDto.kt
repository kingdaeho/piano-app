package com.pianocompanion.api.domain.lesson.dto

import com.pianocompanion.api.domain.lesson.entity.LessonAssignment
import com.pianocompanion.api.domain.lesson.entity.LessonNote
import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import com.pianocompanion.api.domain.piece.entity.Piece
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class CreateLessonNoteRequest(
    @field:NotNull(message = "레슨 날짜는 필수입니다")
    val lessonDate: LocalDate,

    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val content: String? = null,
    val teacherFeedback: String? = null,
    val pieceIds: List<Long> = emptyList(),
    val assignments: List<AssignmentInput> = emptyList(),
)

data class UpdateLessonNoteRequest(
    val lessonDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val content: String? = null,
    val teacherFeedback: String? = null,
    val pieceIds: List<Long>? = null,
    val assignments: List<AssignmentInput>? = null,
)

data class AssignmentInput(
    @field:Size(max = 500, message = "과제 내용은 500자 이하여야 합니다")
    val content: String,
    val isCompleted: Boolean = false,
)

data class LessonNoteView(
    val id: Long,
    val lessonNumber: Int,
    val lessonDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val content: String?,
    val teacherFeedback: String?,
    val pieces: List<PieceSummaryView>,
    val assignments: List<AssignmentView>,
    val createdAt: Instant,
) {
    companion object {
        fun from(note: LessonNote, pieces: List<Piece>): LessonNoteView = LessonNoteView(
            id = note.id,
            lessonNumber = note.lessonNumber,
            lessonDate = note.lessonDate,
            startTime = note.startTime,
            endTime = note.endTime,
            content = note.content,
            teacherFeedback = note.teacherFeedback,
            pieces = pieces.map { PieceSummaryView.from(it) },
            assignments = note.assignments.map { AssignmentView.from(it) },
            createdAt = note.createdAt,
        )
    }
}

data class AssignmentView(
    val id: Long,
    val content: String,
    val isCompleted: Boolean,
    val orderIndex: Int,
) {
    companion object {
        fun from(assignment: LessonAssignment): AssignmentView = AssignmentView(
            id = assignment.id,
            content = assignment.content,
            isCompleted = assignment.isCompleted,
            orderIndex = assignment.orderIndex,
        )
    }
}
