package com.pianocompanion.api.domain.goal.controller

import com.pianocompanion.api.domain.goal.dto.GoalsView
import com.pianocompanion.api.domain.goal.dto.SetDailyGoalRequest
import com.pianocompanion.api.domain.goal.dto.SetWeeklyGoalRequest
import com.pianocompanion.api.domain.goal.service.GoalService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@Tag(name = "목표")
@RestController
@RequestMapping("/api/v1/goals")
class GoalController(
    private val goalService: GoalService,
) {

    @GetMapping
    @Operation(summary = "목표 조회")
    fun getGoals(
        @CurrentUser user: UserPrincipal,
        @RequestParam(required = false) timezone: String?,
    ): ResponseEntity<ApiResponse<GoalsView>> {
        val zoneId = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.of("Asia/Seoul"))
        val goals = goalService.getGoals(user.userId, zoneId)
        return ResponseEntity.ok(ApiResponse.ok(goals))
    }

    @PutMapping("/daily")
    @Operation(summary = "일일 목표 설정")
    fun setDailyGoal(
        @CurrentUser user: UserPrincipal,
        @Valid @RequestBody request: SetDailyGoalRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        goalService.setDailyGoal(user.userId, request)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PutMapping("/weekly")
    @Operation(summary = "주간 목표 설정")
    fun setWeeklyGoal(
        @CurrentUser user: UserPrincipal,
        @Valid @RequestBody request: SetWeeklyGoalRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        goalService.setWeeklyGoal(user.userId, request)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
