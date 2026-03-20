package com.pianocompanion.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.domain.lesson.controller.LessonNoteController
import com.pianocompanion.api.domain.lesson.dto.AssignmentInput
import com.pianocompanion.api.domain.lesson.dto.AssignmentView
import com.pianocompanion.api.domain.lesson.dto.CreateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.dto.LessonNoteView
import com.pianocompanion.api.domain.lesson.dto.UpdateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.service.LessonNoteService
import com.pianocompanion.api.domain.piece.dto.PieceSummaryView
import com.pianocompanion.api.domain.piece.entity.PieceStatus
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
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import java.time.LocalDate

@WebMvcTest(LessonNoteController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class, JwtAuthenticationFilter::class, IntegrationTestConfig::class)
class LessonNoteControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var lessonNoteService: LessonNoteService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val now = Instant.now()
    private val authToken = "test-access-token"

    private val noteView = LessonNoteView(
        id = 1L,
        lessonNumber = 1,
        lessonDate = LocalDate.of(2026, 3, 19),
        startTime = null,
        endTime = null,
        content = "레슨 내용",
        teacherFeedback = "피드백",
        pieces = listOf(
            PieceSummaryView(
                id = 1L,
                title = "체르니 30번",
                composer = "Czerny",
                difficulty = 3,
                status = PieceStatus.PRACTICING,
                progressPercent = 50,
            ),
        ),
        assignments = listOf(
            AssignmentView(id = 1L, content = "과제1", isCompleted = false, orderIndex = 0),
        ),
        createdAt = now,
    )

    @BeforeEach
    fun setUp() {
        every { jwtTokenProvider.validateAccessToken(authToken) } returns UserPrincipal(userId = 1L, email = "test@example.com")
    }

    @Test
    @DisplayName("레슨 노트 작성 - 201 Created")
    fun createNote() {
        every { lessonNoteService.create(1L, any()) } returns noteView

        mockMvc.post("/api/v1/lesson-notes") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                CreateLessonNoteRequest(
                    lessonDate = LocalDate.of(2026, 3, 19),
                    content = "레슨 내용",
                    teacherFeedback = "피드백",
                    pieceIds = listOf(1L),
                    assignments = listOf(AssignmentInput(content = "과제1")),
                ),
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.lessonNumber") { value(1) }
            jsonPath("$.data.content") { value("레슨 내용") }
            jsonPath("$.data.assignments.length()") { value(1) }
        }
    }

    @Test
    @DisplayName("레슨 노트 목록 조회 - 200 OK")
    fun getList() {
        every { lessonNoteService.getList(1L, any()) } returns Pair(
            listOf(noteView),
            PageMeta(total = 1, page = 0, size = 20),
        )

        mockMvc.get("/api/v1/lesson-notes") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.length()") { value(1) }
            jsonPath("$.meta.total") { value(1) }
        }
    }

    @Test
    @DisplayName("레슨 노트 상세 조회 - 200 OK")
    fun getDetail() {
        every { lessonNoteService.getDetail(1L, 1L) } returns noteView

        mockMvc.get("/api/v1/lesson-notes/1") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(1) }
            jsonPath("$.data.pieces.length()") { value(1) }
        }
    }

    @Test
    @DisplayName("레슨 노트 수정 - 200 OK")
    fun updateNote() {
        val updatedView = noteView.copy(content = "수정된 내용")
        every { lessonNoteService.update(1L, 1L, any()) } returns updatedView

        mockMvc.put("/api/v1/lesson-notes/1") {
            header("Authorization", "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateLessonNoteRequest(content = "수정된 내용"),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.content") { value("수정된 내용") }
        }
    }

    @Test
    @DisplayName("레슨 노트 삭제 - 204 No Content")
    fun deleteNote() {
        justRun { lessonNoteService.delete(1L, 1L) }

        mockMvc.delete("/api/v1/lesson-notes/1") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    @DisplayName("존재하지 않는 노트 조회 - 404 Not Found")
    fun getNotFound() {
        every { lessonNoteService.getDetail(1L, 99L) } throws EntityNotFoundException("LessonNote", 99L)

        mockMvc.get("/api/v1/lesson-notes/99") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error.code") { value("LESSONNOTE_NOT_FOUND") }
        }
    }

    @Test
    @DisplayName("과제 상태 토글 - 200 OK")
    fun toggleAssignment() {
        justRun { lessonNoteService.toggleAssignment(1L, 1L, 1L) }

        mockMvc.patch("/api/v1/lesson-notes/1/assignments/1") {
            header("Authorization", "Bearer $authToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }
    }
}
