package com.pianocompanion.api.domain.practice.controller

import com.pianocompanion.api.domain.practice.dto.EndSessionRequest
import com.pianocompanion.api.domain.practice.dto.PracticeSessionView
import com.pianocompanion.api.domain.practice.dto.StartSessionRequest
import com.pianocompanion.api.domain.practice.dto.SwitchPieceRequest
import com.pianocompanion.api.domain.practice.dto.WeeklyStatsView
import com.pianocompanion.api.domain.practice.service.PracticeSessionService
import com.pianocompanion.api.domain.practice.service.PracticeStatsService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId

@Tag(name = "연습 기록")
@RestController
@RequestMapping("/api/v1/practice-sessions")
class PracticeSessionController(
    private val practiceSessionService: PracticeSessionService,
    private val practiceStatsService: PracticeStatsService,
) {

    @PostMapping
    @Operation(summary = "연습 세션 시작")
    fun startSession(
        @CurrentUser user: UserPrincipal,
        @RequestBody(required = false) request: StartSessionRequest?,
    ): ResponseEntity<ApiResponse<PracticeSessionView>> {
        val session = practiceSessionService.startSession(user.userId, request ?: StartSessionRequest())
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(session))
    }

    @PatchMapping("/{id}/switch-piece")
    @Operation(summary = "곡 전환")
    fun switchPiece(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody request: SwitchPieceRequest,
    ): ResponseEntity<ApiResponse<PracticeSessionView>> {
        val session = practiceSessionService.switchPiece(user.userId, id, request)
        return ResponseEntity.ok(ApiResponse.ok(session))
    }

    @PatchMapping("/{id}/end")
    @Operation(summary = "연습 세션 종료")
    fun endSession(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody(required = false) request: EndSessionRequest?,
    ): ResponseEntity<ApiResponse<PracticeSessionView>> {
        val session = practiceSessionService.endSession(user.userId, id, request ?: EndSessionRequest())
        return ResponseEntity.ok(ApiResponse.ok(session))
    }

    @GetMapping
    @Operation(summary = "연습 세션 목록 조회")
    fun getList(
        @CurrentUser user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<ApiResponse<List<PracticeSessionView>>> {
        val (sessions, meta) = practiceSessionService.getList(user.userId, pageable)
        return ResponseEntity.ok(ApiResponse.ok(sessions, meta))
    }

    @GetMapping("/{id}")
    @Operation(summary = "연습 세션 상세 조회")
    fun getDetail(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
    ): ResponseEntity<ApiResponse<PracticeSessionView>> {
        val session = practiceSessionService.getDetail(user.userId, id)
        return ResponseEntity.ok(ApiResponse.ok(session))
    }

    @GetMapping("/stats/weekly")
    @Operation(summary = "주간 통계")
    fun getWeeklyStats(
        @CurrentUser user: UserPrincipal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) timezone: String?,
    ): ResponseEntity<ApiResponse<WeeklyStatsView>> {
        val zoneId = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.of("Asia/Seoul"))
        val stats = practiceStatsService.getWeeklyStats(
            userId = user.userId,
            date = date ?: LocalDate.now(zoneId),
            zoneId = zoneId,
        )
        return ResponseEntity.ok(ApiResponse.ok(stats))
    }
}
