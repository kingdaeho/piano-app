package com.pianocompanion.api.global.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest : FunSpec({
    val handler = GlobalExceptionHandler()

    test("BusinessException 처리 - EntityNotFoundException") {
        val exception = EntityNotFoundException("Piece", 123L)

        val response = handler.handleBusinessException(exception)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.success shouldBe false
        response.body?.error?.code shouldBe "PIECE_NOT_FOUND"
    }

    test("BusinessException 처리 - DuplicateException") {
        val exception = DuplicateException("이미 사용 중인 이메일입니다", "EMAIL_DUPLICATE")

        val response = handler.handleBusinessException(exception)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.error?.code shouldBe "EMAIL_DUPLICATE"
    }

    test("BusinessException 처리 - AuthenticationException") {
        val exception = AuthenticationException("인증 실패")

        val response = handler.handleBusinessException(exception)

        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body?.error?.code shouldBe "AUTHENTICATION_FAILED"
    }

    test("BusinessException 처리 - ForbiddenException") {
        val exception = ForbiddenException("권한이 없습니다")

        val response = handler.handleBusinessException(exception)

        response.statusCode shouldBe HttpStatus.FORBIDDEN
        response.body?.error?.code shouldBe "FORBIDDEN"
    }

    test("BusinessException 처리 - InvalidRequestException") {
        val exception = InvalidRequestException("잘못된 요청입니다")

        val response = handler.handleBusinessException(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.error?.code shouldBe "INVALID_REQUEST"
    }

    test("일반 Exception은 500을 반환한다") {
        val exception = RuntimeException("unexpected error")

        val response = handler.handleException(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.success shouldBe false
        response.body?.error?.code shouldBe "INTERNAL_SERVER_ERROR"
    }
})
