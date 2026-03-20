package com.pianocompanion.api.domain.dashboard.controller

import com.pianocompanion.api.domain.dashboard.dto.DashboardView
import com.pianocompanion.api.domain.dashboard.service.DashboardService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@Tag(name = "대시보드")
@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val dashboardService: DashboardService,
) {

    @GetMapping
    @Operation(summary = "대시보드 데이터 조회")
    fun getDashboard(
        @CurrentUser user: UserPrincipal,
        @RequestParam(required = false) timezone: String?,
    ): ResponseEntity<ApiResponse<DashboardView>> {
        val zoneId = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.of("Asia/Seoul"))
        val dashboard = dashboardService.getDashboard(user.userId, zoneId)
        return ResponseEntity.ok(ApiResponse.ok(dashboard))
    }
}
