package com.pianocompanion.api.domain.user.controller

import com.pianocompanion.api.domain.user.dto.UpdateProfileRequest
import com.pianocompanion.api.domain.user.dto.UserView
import com.pianocompanion.api.domain.user.service.UserService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/me")
    @Operation(summary = "프로필 조회")
    fun getProfile(@CurrentUser user: UserPrincipal): ResponseEntity<ApiResponse<UserView>> {
        val profile = userService.getProfile(user.userId)
        return ResponseEntity.ok(ApiResponse.ok(profile))
    }

    @PatchMapping("/me")
    @Operation(summary = "프로필 수정")
    fun updateProfile(
        @CurrentUser user: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<ApiResponse<UserView>> {
        val profile = userService.updateProfile(user.userId, request)
        return ResponseEntity.ok(ApiResponse.ok(profile))
    }
}
