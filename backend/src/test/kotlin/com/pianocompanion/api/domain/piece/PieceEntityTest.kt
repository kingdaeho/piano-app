package com.pianocompanion.api.domain.piece

import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PieceEntityTest : FunSpec({

    test("NOT_STARTED에서 PRACTICING으로 변경 시 startedAt이 설정된다") {
        val piece = Piece(userId = 1L, title = "테스트곡", status = PieceStatus.NOT_STARTED)
        piece.startedAt.shouldBeNull()

        piece.update(status = PieceStatus.PRACTICING)

        piece.status shouldBe PieceStatus.PRACTICING
        piece.startedAt.shouldNotBeNull()
    }

    test("PRACTICING에서 COMPLETED로 변경 시 completedAt이 설정되고 progress가 100이 된다") {
        val piece = Piece(userId = 1L, title = "테스트곡", status = PieceStatus.PRACTICING, progressPercent = 70)

        piece.update(status = PieceStatus.COMPLETED)

        piece.status shouldBe PieceStatus.COMPLETED
        piece.completedAt.shouldNotBeNull()
        piece.progressPercent shouldBe 100
    }

    test("같은 상태로 변경하면 아무 변화 없다") {
        val piece = Piece(userId = 1L, title = "테스트곡", status = PieceStatus.PRACTICING)

        piece.update(status = PieceStatus.PRACTICING)

        piece.completedAt.shouldBeNull()
    }

    test("이미 startedAt이 있는 상태에서 PRACTICING으로 변경해도 startedAt은 유지된다") {
        val piece = Piece(userId = 1L, title = "테스트곡", status = PieceStatus.ON_HOLD)
        piece.update(status = PieceStatus.PRACTICING)
        val originalStartedAt = piece.startedAt

        piece.update(status = PieceStatus.ON_HOLD)
        piece.update(status = PieceStatus.PRACTICING)

        piece.startedAt shouldBe originalStartedAt
    }

    test("update로 여러 필드를 동시에 변경할 수 있다") {
        val piece = Piece(userId = 1L, title = "원래제목", composer = "원래작곡가")

        piece.update(
            title = "새제목",
            composer = "새작곡가",
            genre = "클래식",
            difficulty = 4,
            progressPercent = 50,
            memo = "메모",
        )

        piece.title shouldBe "새제목"
        piece.composer shouldBe "새작곡가"
        piece.genre shouldBe "클래식"
        piece.difficulty shouldBe 4
        piece.progressPercent shouldBe 50
        piece.memo shouldBe "메모"
    }

    test("null 값으로 update하면 기존 값이 유지된다") {
        val piece = Piece(userId = 1L, title = "원래제목", composer = "원래작곡가")

        piece.update(title = null, composer = null)

        piece.title shouldBe "원래제목"
        piece.composer shouldBe "원래작곡가"
    }

    test("softDelete 호출 시 deletedAt이 설정된다") {
        val piece = Piece(userId = 1L, title = "삭제곡")
        piece.deletedAt.shouldBeNull()

        piece.softDelete()

        piece.deletedAt.shouldNotBeNull()
    }
})
