package com.pianocompanion.api.domain.piece

import com.pianocompanion.api.domain.piece.dto.CreatePieceRequest
import com.pianocompanion.api.domain.piece.dto.UpdatePieceRequest
import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.piece.service.PieceService
import com.pianocompanion.api.global.exception.EntityNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PieceServiceTest : BehaviorSpec({
    val pieceRepository = mockk<PieceRepository>()
    val pieceService = PieceService(pieceRepository)

    val userId = 1L

    given("곡 등록") {
        val request = CreatePieceRequest(
            title = "체르니 30번 - 8번",
            composer = "Carl Czerny",
            genre = "클래식",
            difficulty = 3,
            status = PieceStatus.PRACTICING,
        )

        `when`("유효한 요청으로 등록하면") {
            every { pieceRepository.save(any()) } answers {
                firstArg<Piece>()
            }

            val result = pieceService.create(userId, request)

            then("곡 정보를 반환한다") {
                result.title shouldBe "체르니 30번 - 8번"
                result.composer shouldBe "Carl Czerny"
                result.difficulty shouldBe 3
                result.status shouldBe PieceStatus.PRACTICING
            }
        }
    }

    given("곡 목록 조회") {
        val pageable = PageRequest.of(0, 20)

        `when`("사용자의 곡 목록을 조회하면") {
            val pieces = listOf(
                Piece(userId = userId, title = "곡1", status = PieceStatus.PRACTICING),
                Piece(userId = userId, title = "곡2", status = PieceStatus.NOT_STARTED),
            )
            every { pieceRepository.findByUserIdAndStatus(userId, null, pageable) } returns PageImpl(pieces)

            val (list, meta) = pieceService.getList(userId, null, pageable)

            then("곡 목록과 페이지 메타를 반환한다") {
                list.size shouldBe 2
                meta.total shouldBe 2
            }
        }

        `when`("상태 필터를 적용하면") {
            val pieces = listOf(
                Piece(userId = userId, title = "곡1", status = PieceStatus.PRACTICING),
            )
            every {
                pieceRepository.findByUserIdAndStatus(userId, PieceStatus.PRACTICING, pageable)
            } returns PageImpl(pieces)

            val (list, _) = pieceService.getList(userId, PieceStatus.PRACTICING, pageable)

            then("필터된 곡 목록을 반환한다") {
                list.size shouldBe 1
                list.first().status shouldBe PieceStatus.PRACTICING
            }
        }
    }

    given("곡 수정") {
        val pieceId = 10L
        val piece = Piece(userId = userId, title = "원래 제목", status = PieceStatus.NOT_STARTED)

        `when`("존재하는 곡을 수정하면") {
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(pieceId, userId) } returns piece

            val request = UpdatePieceRequest(
                title = "수정된 제목",
                status = PieceStatus.PRACTICING,
                progressPercent = 30,
            )
            val result = pieceService.update(userId, pieceId, request)

            then("수정된 곡 정보를 반환한다") {
                result.title shouldBe "수정된 제목"
                result.status shouldBe PieceStatus.PRACTICING
                result.progressPercent shouldBe 30
            }
        }

        `when`("존재하지 않는 곡을 수정하면") {
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId) } returns null

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    pieceService.update(userId, 99L, UpdatePieceRequest(title = "수정"))
                }
            }
        }
    }

    given("곡 삭제") {
        val pieceId = 10L
        val piece = Piece(userId = userId, title = "삭제할 곡")

        `when`("곡을 삭제하면") {
            every { pieceRepository.findByIdAndUserIdAndDeletedAtIsNull(pieceId, userId) } returns piece

            pieceService.delete(userId, pieceId)

            then("소프트 삭제가 수행된다") {
                piece.deletedAt shouldBe piece.deletedAt
            }
        }
    }
})
