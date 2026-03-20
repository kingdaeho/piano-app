package com.pianocompanion.api.domain.lesson.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "lesson_assignments")
class LessonAssignment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_note_id", nullable = false)
    val lessonNote: LessonNote,

    @Column(nullable = false, length = 500)
    var content: String,

    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,
) : BaseEntity() {

    fun toggleCompletion() {
        this.isCompleted = !this.isCompleted
    }
}
