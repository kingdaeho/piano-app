package com.pianocompanion.api.domain.lesson

import com.pianocompanion.api.domain.lesson.dto.AssignmentInput
import com.pianocompanion.api.domain.lesson.dto.CreateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.dto.UpdateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.entity.LessonAssignment
import com.pianocompanion.api.domain.lesson.entity.LessonNote
import com.pianocompanion.api.domain.lesson.repository.LessonNoteRepository
import com.pianocompanion.api.domain.lesson.service.LessonNoteService
import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.global.exception.EntityNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class LessonNoteServiceTest : BehaviorSpec({
    val lessonNoteRepository = mockk<LessonNoteRepository>()
    val pieceRepository = mockk<PieceRepository>()

    val service = LessonNoteService(lessonNoteRepository, pieceRepository)
    val userId = 1L

    given("레슨 노트 작성") {
        `when`("유효한 요청으로 작성하면") {
            every { lessonNoteRepository.getNextLessonNumber(userId) } returns 1
            every { lessonNoteRepository.save(any()) } answers { firstArg() }
            every { pieceRepository.findAllById(listOf(1L)) } returns listOf(
                Piece(userId = userId, title = "테스트곡", status = PieceStatus.PRACTICING),
            )

            val request = CreateLessonNoteRequest(
                lessonDate = LocalDate.of(2026, 3, 19),
                content = "레슨 내용",
                teacherFeedback = "피드백",
                pieceIds = listOf(1L),
                assignments = listOf(
                    AssignmentInput(content = "과제1"),
                    AssignmentInput(content = "과제2"),
                ),
            )
            val result = service.create(userId, request)

            then("레슨 노트를 반환한다") {
                result.lessonNumber shouldBe 1
                result.lessonDate shouldBe LocalDate.of(2026, 3, 19)
                result.content shouldBe "레슨 내용"
                result.assignments.size shouldBe 2
            }
        }
    }

    given("레슨 노트 조회") {
        `when`("존재하지 않는 노트를 조회하면") {
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.getDetail(userId, 99L)
                }
            }
        }
    }

    given("레슨 노트 삭제") {
        `when`("존재하는 노트를 삭제하면") {
            val note = LessonNote(
                userId = userId,
                lessonNumber = 1,
                lessonDate = LocalDate.now(),
            )
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId) } returns note

            service.delete(userId, 1L)

            then("소프트 삭제가 수행된다") {
                note.deletedAt shouldNotBe null
            }
        }

        `when`("존재하지 않는 노트를 삭제하면") {
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.delete(userId, 99L)
                }
            }
        }
    }

    given("레슨 노트 수정") {
        `when`("유효한 요청으로 수정하면") {
            val note = LessonNote(
                userId = userId,
                lessonNumber = 1,
                lessonDate = LocalDate.of(2026, 3, 10),
                content = "원래 내용",
            )
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId) } returns note
            every { pieceRepository.findAllById(emptyList()) } returns emptyList()

            val request = UpdateLessonNoteRequest(
                content = "수정된 내용",
                teacherFeedback = "피드백 추가",
            )
            val result = service.update(userId, 1L, request)

            then("수정된 내용이 반환된다") {
                result.content shouldBe "수정된 내용"
                result.teacherFeedback shouldBe "피드백 추가"
                result.lessonDate shouldBe LocalDate.of(2026, 3, 10)
            }
        }

        `when`("존재하지 않는 노트를 수정하면") {
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.update(userId, 99L, UpdateLessonNoteRequest(content = "수정"))
                }
            }
        }
    }

    given("과제 상태 토글") {
        `when`("존재하는 과제를 토글하면") {
            val note = LessonNote(
                userId = userId,
                lessonNumber = 1,
                lessonDate = LocalDate.now(),
            )
            val assignment = LessonAssignment(
                lessonNote = note,
                content = "과제1",
                isCompleted = false,
                orderIndex = 0,
            )
            note.assignments.add(assignment)
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId) } returns note

            service.toggleAssignment(userId, 1L, 0L)

            then("과제 상태가 변경된다") {
                assignment.isCompleted shouldBe true
            }
        }

        `when`("존재하지 않는 과제를 토글하면") {
            val note = LessonNote(
                userId = userId,
                lessonNumber = 1,
                lessonDate = LocalDate.now(),
            )
            every { lessonNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId) } returns note

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.toggleAssignment(userId, 1L, 999L)
                }
            }
        }
    }

    given("레슨 노트 작성 - 곡 없이") {
        `when`("곡 없이 노트를 작성하면") {
            every { lessonNoteRepository.getNextLessonNumber(userId) } returns 5
            every { lessonNoteRepository.save(any()) } answers { firstArg() }
            every { pieceRepository.findAllById(emptyList()) } returns emptyList()

            val request = CreateLessonNoteRequest(
                lessonDate = LocalDate.of(2026, 3, 20),
                content = "연습 내용",
                pieceIds = emptyList(),
                assignments = emptyList(),
            )
            val result = service.create(userId, request)

            then("곡 없이 노트가 생성된다") {
                result.lessonNumber shouldBe 5
                result.pieces shouldBe emptyList()
                result.assignments shouldBe emptyList()
            }
        }
    }
})
