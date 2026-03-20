package com.pianocompanion.api.domain.lesson.controller

import com.pianocompanion.api.domain.lesson.dto.CreateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.dto.LessonNoteView
import com.pianocompanion.api.domain.lesson.dto.UpdateLessonNoteRequest
import com.pianocompanion.api.domain.lesson.service.LessonNoteService
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "레슨 노트")
@RestController
@RequestMapping("/api/v1/lesson-notes")
class LessonNoteController(
    private val lessonNoteService: LessonNoteService,
) {

    @PostMapping
    @Operation(summary = "레슨 노트 작성")
    fun create(
        @CurrentUser user: UserPrincipal,
        @Valid @RequestBody request: CreateLessonNoteRequest,
    ): ResponseEntity<ApiResponse<LessonNoteView>> {
        val note = lessonNoteService.create(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(note))
    }

    @GetMapping
    @Operation(summary = "레슨 노트 목록 조회")
    fun getList(
        @CurrentUser user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<ApiResponse<List<LessonNoteView>>> {
        val (notes, meta) = lessonNoteService.getList(user.userId, pageable)
        return ResponseEntity.ok(ApiResponse.ok(notes, meta))
    }

    @GetMapping("/{id}")
    @Operation(summary = "레슨 노트 상세 조회")
    fun getDetail(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
    ): ResponseEntity<ApiResponse<LessonNoteView>> {
        val note = lessonNoteService.getDetail(user.userId, id)
        return ResponseEntity.ok(ApiResponse.ok(note))
    }

    @PutMapping("/{id}")
    @Operation(summary = "레슨 노트 수정")
    fun update(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLessonNoteRequest,
    ): ResponseEntity<ApiResponse<LessonNoteView>> {
        val note = lessonNoteService.update(user.userId, id, request)
        return ResponseEntity.ok(ApiResponse.ok(note))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "레슨 노트 삭제")
    fun delete(
        @CurrentUser user: UserPrincipal,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        lessonNoteService.delete(user.userId, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{noteId}/assignments/{assignmentId}")
    @Operation(summary = "과제 상태 토글")
    fun toggleAssignment(
        @CurrentUser user: UserPrincipal,
        @PathVariable noteId: Long,
        @PathVariable assignmentId: Long,
    ): ResponseEntity<ApiResponse<Nothing>> {
        lessonNoteService.toggleAssignment(user.userId, noteId, assignmentId)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
