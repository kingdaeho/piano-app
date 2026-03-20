package com.pianocompanion.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.domain.practice.controller.PracticeSessionController
import com.pianocompanion.api.domain.practice.dto.EndSessionRequest
import com.pianocompanion.api.domain.practice.dto.PracticeSessionView
import com.pianocompanion.api.domain.practice.dto.StartSessionRequest
import com.pianocompanion.api.domain.practice.dto.SwitchPieceRequest
import com.pianocompanion.api.domain.practice.dto.WeeklyStatsView
import com.pianocompanion.api.domain.practice.entity.Mood
import com.pianocompanion.api.domain.practice.service.PracticeSessionService
import com.pianocompanion.api.domain.practice.service.PracticeStatsService
import com.pianocompanion.api.global.common.PageMeta
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.GlobalExceptionHandler
import com.pianocompanion.api.global.exception.InvalidRequestException
import com.pianocompanion.api.global.security.JwtAuthenticationFilter
import com.pianocompanion.api.global.security.SecurityConfig
import com.pianocompanion.api.global.security.UserPrincipal
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.LocalDate

@WebMvcTest(PracticeSessionController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class, JwtAuthenticationFilter::class, IntegrationTestConfig::class)
class PracticeSessionControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var practiceSessionService: PracticeSessionService

    @MockkBean
    private lateinit var practiceStatsService: PracticeStatsService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val now = Instant.now()
    private val authToken = "test-access-token"

    private val sessionView = PracticeSessionView(
        id = 1L,
        startedAt = now,
        endedAt = null,
        totalDurationSeconds = 0,
        memo = null,
        mood = null,
        pieces = emptyList(),
    )

    @BeforeEach
    fun setUp() {
        every { jwtTokenProvider.validateAccessToken(authToken) } returns UserPrincipal(userId = 1L, email = "test@example.com")
    }

    @Test
    @DisplayName("연습 세션 시작 - 201 Created")
    fun startSession() {
        every { practiceSessionService.startSession(1L, any()) } returns sessionView

        mockMvc.post("/api/v1/practice-sessions") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(StartSessionRequest())
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.id") { value(1) }
        }
    }

    @Test
    @DisplayName("곡 전환 - 200 OK")
    fun switchPiece() {
        every { practiceSessionService.switchPiece(1L, 1L, any()) } returns sessionView

        mockMvc.patch("/api/v1/practice-sessions/1/switch-piece") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SwitchPieceRequest(pieceId = 2L))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }
    }

    @Test
    @DisplayName("연습 세션 종료 - 200 OK")
    fun endSession() {
        val endedView = sessionView.copy(
            endedAt = now.plusSeconds(3600),
            totalDurationSeconds = 3600,
            memo = "좋은 연습",
            mood = Mood.GOOD,
        )
        every { practiceSessionService.endSession(1L, 1L, any()) } returns endedView

        mockMvc.patch("/api/v1/practice-sessions/1/end") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(EndSessionRequest(memo = "좋은 연습", mood = Mood.GOOD))
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.totalDurationSeconds") { value(3600) }
            jsonPath("$.data.memo") { value("좋은 연습") }
            jsonPath("$.data.mood") { value("GOOD") }
        }
    }

    @Test
    @DisplayName("이미 종료된 세션 종료 시도 - 400 Bad Request")
    fun endAlreadyEndedSession() {
        every {
            practiceSessionService.endSession(1L, 1L, any())
        } throws InvalidRequestException("이미 종료된 세션입니다")

        mockMvc.patch("/api/v1/practice-sessions/1/end") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(EndSessionRequest())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_REQUEST") }
        }
    }

    @Test
    @DisplayName("세션 목록 조회 - 200 OK")
    fun getList() {
        every { practiceSessionService.getList(1L, any()) } returns Pair(
            listOf(sessionView),
            PageMeta(total = 1, page = 0, size = 20),
        )

        mockMvc.get("/api/v1/practice-sessions") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.length()") { value(1) }
        }
    }

    @Test
    @DisplayName("존재하지 않는 세션 조회 - 404 Not Found")
    fun getNotFound() {
        every {
            practiceSessionService.getDetail(1L, 99L)
        } throws EntityNotFoundException("PracticeSession", 99L)

        mockMvc.get("/api/v1/practice-sessions/99") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @DisplayName("주간 통계 조회 - 200 OK")
    fun getWeeklyStats() {
        val statsView = WeeklyStatsView(
            weekStart = LocalDate.of(2026, 3, 16),
            weekEnd = LocalDate.of(2026, 3, 22),
            totalDurationSeconds = 7200,
            practiceDays = 3,
            dailyStats = emptyList(),
            pieceStats = emptyList(),
            previousWeekDurationSeconds = 3600,
            changePercent = 100.0,
        )
        every { practiceStatsService.getWeeklyStats(1L, any(), any()) } returns statsView

        mockMvc.get("/api/v1/practice-sessions/stats/weekly") {
            header("Authorization", "Bearer $authToken")
            param("timezone", "Asia/Seoul")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.totalDurationSeconds") { value(7200) }
            jsonPath("$.data.practiceDays") { value(3) }
        }
    }
}
