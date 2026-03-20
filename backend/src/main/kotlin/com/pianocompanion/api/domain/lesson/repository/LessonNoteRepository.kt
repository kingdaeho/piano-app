package com.pianocompanion.api.domain.lesson.repository

import com.pianocompanion.api.domain.lesson.entity.LessonNote
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LessonNoteRepository : JpaRepository<LessonNote, Long> {

    fun findByIdAndUserIdAndDeletedAtIsNull(id: Long, userId: Long): LessonNote?

    fun findByUserIdAndDeletedAtIsNullOrderByLessonDateDesc(userId: Long, pageable: Pageable): Page<LessonNote>

    @Query(
        """
        SELECT COALESCE(MAX(ln.lessonNumber), 0) + 1
        FROM LessonNote ln
        WHERE ln.userId = :userId AND ln.deletedAt IS NULL
        """,
    )
    fun getNextLessonNumber(userId: Long): Int

    @Query(
        """
        SELECT ln FROM LessonNote ln
        WHERE ln.userId = :userId AND ln.deletedAt IS NULL
        ORDER BY ln.lessonDate DESC, ln.lessonNumber DESC
        LIMIT 1
        """,
    )
    fun findLatestByUserId(userId: Long): LessonNote?
}
