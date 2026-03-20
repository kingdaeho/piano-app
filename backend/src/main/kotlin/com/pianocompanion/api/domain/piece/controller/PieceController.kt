package com.pianocompanion.api.domain.piece.controller

import com.pianocompanion.api.domain.piece.dto.CreatePieceRequest
import com.pianocompanion.api.domain.piece.dto.PieceView
import com.pianocompanion.api.domain.piece.dto.UpdatePieceRequest
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.service.PieceService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "곡 관리")
@RestController
@RequestMapping("/api/v1/pieces")
class PieceController(
    private val pieceService: PieceService,
) {

    @PostMapping
    @Operation(summary = "곡 등록")
    fun create(
        @CurrentUser user: UserPrincipal,
        @Valid @RequestBody request: CreatePieceRequest,
    ): ResponseEntity<ApiResponse<PieceView>> {
        val piece = pieceService.create(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(piece))
    }

    @GetMapping
    @Operation(summary = "곡 목록 조회")
    fun getList(
        @CurrentUser user: UserPrincipal,
        @RequestParam(required = false) status: PieceStatus?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<ApiResponse<List<PieceView>>> {
        val (pieces, meta) = pieceService.getList(user.userId, status, pageable)
        return ResponseEntity.ok(ApiResponse.ok(pieces, meta))
    }

    @GetMapping("/{id}")
    @Operation(summary = "곡 상세 조회")
    fun getDetail(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
    ): ResponseEntity<ApiResponse<PieceView>> {
        val piece = pieceService.getDetail(user.userId, id)
        return ResponseEntity.ok(ApiResponse.ok(piece))
    }

    @PutMapping("/{id}")
    @Operation(summary = "곡 수정")
    fun update(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePieceRequest,
    ): ResponseEntity<ApiResponse<PieceView>> {
        val piece = pieceService.update(user.userId, id, request)
        return ResponseEntity.ok(ApiResponse.ok(piece))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "곡 삭제")
    fun delete(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        pieceService.delete(user.userId, id)
        return ResponseEntity.noContent().build()
    }
}
