package com.pianocompanion.api.domain.piece.dto

import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreatePieceRequest(
    @field:NotBlank(message = "곡명은 필수입니다")
    @field:Size(max = 200, message = "곡명은 200자 이하여야 합니다")
    val title: String,

    @field:Size(max = 100, message = "작곡가는 100자 이하여야 합니다")
    val composer: String? = null,

    @field:Size(max = 50, message = "장르는 50자 이하여야 합니다")
    val genre: String? = null,

    @field:Min(1, message = "난이도는 1 이상이어야 합니다")
    @field:Max(5, message = "난이도는 5 이하여야 합니다")
    val difficulty: Int? = null,

    val status: PieceStatus = PieceStatus.NOT_STARTED,

    val memo: String? = null,
)

data class UpdatePieceRequest(
    @field:Size(max = 200, message = "곡명은 200자 이하여야 합니다")
    val title: String? = null,

    @field:Size(max = 100, message = "작곡가는 100자 이하여야 합니다")
    val composer: String? = null,

    @field:Size(max = 50, message = "장르는 50자 이하여야 합니다")
    val genre: String? = null,

    @field:Min(1, message = "난이도는 1 이상이어야 합니다")
    @field:Max(5, message = "난이도는 5 이하여야 합니다")
    val difficulty: Int? = null,

    val status: PieceStatus? = null,

    @field:Min(0, message = "진행도는 0 이상이어야 합니다")
    @field:Max(100, message = "진행도는 100 이하여야 합니다")
    val progressPercent: Int? = null,

    val memo: String? = null,
)

data class PieceView(
    val id: Long,
    val title: String,
    val composer: String?,
    val genre: String?,
    val difficulty: Int?,
    val status: PieceStatus,
    val progressPercent: Int,
    val memo: String?,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant,
) {
    companion object {
        fun from(piece: Piece): PieceView = PieceView(
            id = piece.id,
            title = piece.title,
            composer = piece.composer,
            genre = piece.genre,
            difficulty = piece.difficulty,
            status = piece.status,
            progressPercent = piece.progressPercent,
            memo = piece.memo,
            startedAt = piece.startedAt,
            completedAt = piece.completedAt,
            createdAt = piece.createdAt,
        )
    }
}

data class PieceSummaryView(
    val id: Long,
    val title: String,
    val composer: String?,
    val difficulty: Int?,
    val status: PieceStatus,
    val progressPercent: Int,
) {
    companion object {
        fun from(piece: Piece): PieceSummaryView = PieceSummaryView(
            id = piece.id,
            title = piece.title,
            composer = piece.composer,
            difficulty = piece.difficulty,
            status = piece.status,
            progressPercent = piece.progressPercent,
        )
    }
}
