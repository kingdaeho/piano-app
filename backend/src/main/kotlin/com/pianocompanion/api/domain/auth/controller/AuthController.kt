package com.pianocompanion.api.domain.auth.controller

import com.pianocompanion.api.domain.auth.dto.AuthResponse
import com.pianocompanion.api.domain.auth.dto.LoginRequest
import com.pianocompanion.api.domain.auth.dto.RefreshRequest
import com.pianocompanion.api.domain.auth.dto.SignupRequest
import com.pianocompanion.api.domain.auth.dto.TokenResponse
import com.pianocompanion.api.domain.auth.service.AuthService
import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.security.CurrentUser
import com.pianocompanion.api.global.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response))
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val response = authService.refresh(request)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    fun logout(
        @CurrentUser user: UserPrincipal,
        @RequestBody(required = false) request: RefreshRequest?,
    ): ResponseEntity<ApiResponse<Nothing>> {
        authService.logout(user.userId, request?.refreshToken)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
