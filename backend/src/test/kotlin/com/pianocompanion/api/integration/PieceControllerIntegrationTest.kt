package com.pianocompanion.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.domain.piece.controller.PieceController
import com.pianocompanion.api.domain.piece.dto.CreatePieceRequest
import com.pianocompanion.api.domain.piece.dto.PieceView
import com.pianocompanion.api.domain.piece.dto.UpdatePieceRequest
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.service.PieceService
import com.pianocompanion.api.global.common.PageMeta
import com.pianocompanion.api.global.exception.EntityNotFoundException
import com.pianocompanion.api.global.exception.GlobalExceptionHandler
import com.pianocompanion.api.global.security.JwtAuthenticationFilter
import com.pianocompanion.api.global.security.SecurityConfig
import com.pianocompanion.api.global.security.UserPrincipal
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant

@WebMvcTest(PieceController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class, JwtAuthenticationFilter::class, IntegrationTestConfig::class)
class PieceControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var pieceService: PieceService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val now = Instant.now()
    private val authToken = "test-access-token"

    private val pieceView = PieceView(
        id = 1L,
        title = "체르니 30번 - 8번",
        composer = "Carl Czerny",
        genre = "클래식",
        difficulty = 3,
        status = PieceStatus.PRACTICING,
        progressPercent = 40,
        memo = null,
        startedAt = now,
        completedAt = null,
        createdAt = now,
    )

    @BeforeEach
    fun setUp() {
        every { jwtTokenProvider.validateAccessToken(authToken) } returns UserPrincipal(userId = 1L, email = "test@example.com")
    }

    @Test
    @DisplayName("곡 등록 - 201 Created")
    fun createPiece() {
        every { pieceService.create(1L, any()) } returns pieceView

        mockMvc.post("/api/v1/pieces") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreatePieceRequest(title = "체르니 30번 - 8번", composer = "Carl Czerny", difficulty = 3),
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.title") { value("체르니 30번 - 8번") }
            jsonPath("$.data.composer") { value("Carl Czerny") }
        }
    }

    @Test
    @DisplayName("곡 목록 조회 - 200 OK")
    fun getList() {
        every { pieceService.getList(1L, null, any()) } returns Pair(
            listOf(pieceView),
            PageMeta(total = 1, page = 0, size = 20),
        )

        mockMvc.get("/api/v1/pieces") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.length()") { value(1) }
            jsonPath("$.meta.total") { value(1) }
        }
    }

    @Test
    @DisplayName("곡 상세 조회 - 200 OK")
    fun getDetail() {
        every { pieceService.getDetail(1L, 1L) } returns pieceView

        mockMvc.get("/api/v1/pieces/1") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(1) }
            jsonPath("$.data.title") { value("체르니 30번 - 8번") }
        }
    }

    @Test
    @DisplayName("곡 수정 - 200 OK")
    fun updatePiece() {
        val updatedView = pieceView.copy(title = "수정된 제목", progressPercent = 70)
        every { pieceService.update(1L, 1L, any()) } returns updatedView

        mockMvc.put("/api/v1/pieces/1") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdatePieceRequest(title = "수정된 제목", progressPercent = 70),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.title") { value("수정된 제목") }
            jsonPath("$.data.progressPercent") { value(70) }
        }
    }

    @Test
    @DisplayName("곡 삭제 - 204 No Content")
    fun deletePiece() {
        justRun { pieceService.delete(1L, 1L) }

        mockMvc.delete("/api/v1/pieces/1") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    @DisplayName("존재하지 않는 곡 조회 - 404 Not Found")
    fun getNotFound() {
        every { pieceService.getDetail(1L, 99L) } throws EntityNotFoundException("Piece", 99L)

        mockMvc.get("/api/v1/pieces/99") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("PIECE_NOT_FOUND") }
        }
    }

    @Test
    @DisplayName("인증 없이 곡 조회 - 401 Unauthorized")
    fun unauthorizedAccess() {
        every { jwtTokenProvider.validateAccessToken(any()) } returns null

        mockMvc.get("/api/v1/pieces") {
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
