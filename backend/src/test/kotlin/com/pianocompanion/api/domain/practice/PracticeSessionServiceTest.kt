package com.pianocompanion.api.domain.practice

import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.dto.EndSessionRequest
import com.pianocompanion.api.domain.practice.dto.StartSessionRequest
import com.pianocompanion.api.domain.practice.dto.SwitchPieceRequest
import com.pianocompanion.api.domain.practice.entity.Mood
import com.pianocompanion.api.domain.practice.entity.PracticeSession
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.domain.practice.service.PracticeSessionService
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.InvalidRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class PracticeSessionServiceTest : BehaviorSpec({
    val practiceSessionRepository = mockk<PracticeSessionRepository>()
    val pieceRepository = mockk<PieceRepository>()

    val service = PracticeSessionService(practiceSessionRepository, pieceRepository)
    val userId = 1L

    given("연습 세션 시작") {
        `when`("곡 없이 시작하면") {
            every { practiceSessionRepository.save(any()) } answers { firstArg() }
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()

            val result = service.startSession(userId, StartSessionRequest())

            then("빈 세션이 생성된다") {
                result.pieces shouldBe emptyList()
                result.endedAt shouldBe null
            }
        }

        `when`("곡과 함께 시작하면") {
            val piece = Piece(userId = userId, title = "테스트곡", status = PieceStatus.PRACTICING)
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId) } returns piece
            every { practiceSessionRepository.save(any()) } answers { firstArg() }
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns listOf(piece)

            val result = service.startSession(userId, StartSessionRequest(pieceId = 1L))

            then("곡이 포함된 세션이 생성된다") {
                result.pieces.size shouldBe 1
            }
        }
    }

    given("연습 세션 종료") {
        `when`("진행 중인 세션을 종료하면") {
            val session = PracticeSession(userId = userId, startedAt = Instant.now().minusSeconds(3600))
            every { practiceSessionRepository.findByIdAndUserId(1L, userId) } returns session
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()

            val result = service.endSession(userId, 1L, EndSessionRequest(memo = "좋은 연습", mood = Mood.GOOD))

            then("세션이 종료되고 통계가 저장된다") {
                result.endedAt shouldNotBe null
                result.memo shouldBe "좋은 연습"
                result.mood shouldBe Mood.GOOD
                result.totalDurationSeconds shouldNotBe 0
            }
        }

        `when`("이미 종료된 세션을 종료하면") {
            val session = PracticeSession(
                userId = userId,
                startedAt = Instant.now().minusSeconds(3600),
            ).apply { end(null, null) }
            every { practiceSessionRepository.findByIdAndUserId(2L, userId) } returns session

            then("InvalidRequestException이 발생한다") {
                shouldThrow<InvalidRequestException> {
                    service.endSession(userId, 2L, EndSessionRequest())
                }
            }
        }
    }

    given("곡 전환") {
        `when`("존재하지 않는 곡으로 전환하면") {
            val session = PracticeSession(userId = userId, startedAt = Instant.now())
            every { practiceSessionRepository.findByIdAndUserId(1L, userId) } returns session
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.switchPiece(userId, 1L, SwitchPieceRequest(pieceId = 99L))
                }
            }
        }

        `when`("이미 종료된 세션에서 곡을 전환하면") {
            val session = PracticeSession(
                userId = userId,
                startedAt = Instant.now().minusSeconds(3600),
            ).apply { end(null, null) }
            every { practiceSessionRepository.findByIdAndUserId(3L, userId) } returns session

            then("InvalidRequestException이 발생한다") {
                shouldThrow<InvalidRequestException> {
                    service.switchPiece(userId, 3L, SwitchPieceRequest(pieceId = 1L))
                }
            }
        }
    }

    given("존재하지 않는 세션 접근") {
        `when`("존재하지 않는 세션을 종료하면") {
            every { practiceSessionRepository.findByIdAndUserId(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.endSession(userId, 99L, EndSessionRequest())
                }
            }
        }

        `when`("존재하지 않는 세션을 조회하면") {
            every { practiceSessionRepository.findByIdAndUserId(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    service.getDetail(userId, 99L)
                }
            }
        }
    }

    given("세션 목록 조회") {
        `when`("연습 기록이 없는 사용자가 조회하면") {
            every {
                practiceSessionRepository.findByUserIdOrderByStartedAtDesc(eq(userId), any())
            } returns org.springframework.data.domain.PageImpl(emptyList())
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()

            val (list, meta) = service.getList(userId, org.springframework.data.domain.PageRequest.of(0, 20))

            then("빈 목록을 반환한다") {
                list shouldBe emptyList()
                meta.total shouldBe 0
            }
        }
    }
})
