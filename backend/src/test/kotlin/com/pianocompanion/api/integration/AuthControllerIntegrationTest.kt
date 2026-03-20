package com.pianocompanion.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.pianocompanion.api.domain.auth.controller.AuthController
import com.pianocompanion.api.domain.auth.dto.AuthResponse
import com.pianocompanion.api.domain.auth.dto.LoginRequest
import com.pianocompanion.api.domain.auth.dto.SignupRequest
import com.pianocompanion.api.domain.auth.dto.TokenResponse
import com.pianocompanion.api.domain.auth.service.AuthService
import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.domain.user.dto.UserView
import com.pianocompanion.api.domain.user.entity.ExperienceLevel
import com.pianocompanion.api.global.exception.AuthenticationException
import com.pianocompanion.api.global.exception.DuplicateException
import com.pianocompanion.api.global.exception.GlobalExceptionHandler
import com.pianocompanion.api.global.security.JwtAuthenticationFilter
import com.pianocompanion.api.global.security.SecurityConfig
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class, JwtAuthenticationFilter::class, IntegrationTestConfig::class)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val userView = UserView(
        id = 1L,
        email = "test@example.com",
        name = "테스트",
        profileImageUrl = null,
        experienceLevel = ExperienceLevel.BEGINNER,
        dailyGoalMinutes = 60,
        weeklyGoalDays = 5,
        weeklyGoalMinutes = 300,
    )

    @Test
    @DisplayName("회원가입 성공 - 201 Created")
    fun signupSuccess() {
        val request = SignupRequest(
            email = "test@example.com",
            password = "password123",
            name = "테스트",
        )
        every { authService.signup(any()) } returns AuthResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            user = userView,
        )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { value("access_token") }
            jsonPath("$.data.user.email") { value("test@example.com") }
        }
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    fun signupDuplicate() {
        every { authService.signup(any()) } throws DuplicateException("이미 사용 중인 이메일입니다", "EMAIL_DUPLICATE")

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                SignupRequest(email = "dup@example.com", password = "password123", name = "테스트"),
            )
        }.andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("EMAIL_DUPLICATE") }
        }
    }

    @Test
    @DisplayName("로그인 성공 - 200 OK")
    fun loginSuccess() {
        every { authService.login(any()) } returns AuthResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            user = userView,
        )

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(email = "test@example.com", password = "password123"),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { value("access_token") }
        }
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    fun loginFailed() {
        every { authService.login(any()) } throws AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(email = "wrong@example.com", password = "wrongpass"),
            )
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
        }
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 200 OK")
    fun refreshSuccess() {
        every { authService.refresh(any()) } returns TokenResponse(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
        )

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken": "old_refresh_token"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.accessToken") { value("new_access_token") }
            jsonPath("$.data.refreshToken") { value("new_refresh_token") }
        }
    }

    @Test
    @DisplayName("인증 없이 보호된 엔드포인트 접근 - 401 Unauthorized")
    fun unauthenticatedAccess() {
        every { jwtTokenProvider.validateAccessToken(any()) } returns null

        mockMvc.post("/api/v1/users/me") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
