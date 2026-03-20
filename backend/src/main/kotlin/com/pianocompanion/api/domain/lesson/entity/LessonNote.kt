package com.pianocompanion.api.domain.lesson.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "lesson_notes")
class LessonNote(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "lesson_number", nullable = false)
    var lessonNumber: Int,

    @Column(name = "lesson_date", nullable = false)
    var lessonDate: LocalDate,

    @Column(name = "start_time")
    var startTime: LocalTime? = null,

    @Column(name = "end_time")
    var endTime: LocalTime? = null,

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    var teacherFeedback: String? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @OneToMany(mappedBy = "lessonNote", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    val assignments: MutableList<LessonAssignment> = mutableListOf(),

    @OneToMany(mappedBy = "lessonNote", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val notePieces: MutableList<LessonNotePiece> = mutableListOf(),
) : BaseEntity() {

    fun update(
        lessonDate: LocalDate? = null,
        startTime: LocalTime? = null,
        endTime: LocalTime? = null,
        content: String? = null,
        teacherFeedback: String? = null,
    ) {
        lessonDate?.let { this.lessonDate = it }
        startTime?.let { this.startTime = it }
        endTime?.let { this.endTime = it }
        content?.let { this.content = it }
        teacherFeedback?.let { this.teacherFeedback = it }
    }

    fun replaceAssignments(newAssignments: List<LessonAssignment>) {
        assignments.clear()
        assignments.addAll(newAssignments)
    }

    fun replacePieceIds(pieceIds: List<Long>) {
        notePieces.clear()
        pieceIds.forEach { pieceId ->
            notePieces.add(LessonNotePiece(lessonNote = this, pieceId = pieceId))
        }
    }

    fun softDelete() {
        this.deletedAt = Instant.now()
    }
}
