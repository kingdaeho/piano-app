package com.pianocompanion.api.global.exception

import com.pianocompanion.api.global.common.ApiResponse
import com.pianocompanion.api.global.common.ErrorDetail
import com.pianocompanion.api.global.common.getLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = getLogger()

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business exception: [{}] {}", e.errorCode, e.message)
        return ResponseEntity
            .status(e.httpStatus)
            .body(ApiResponse.error(e.errorCode, e.message ?: "알 수 없는 오류가 발생했습니다"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation failed: {}", details)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse(
                    success = false,
                    error = ErrorDetail(
                        code = "VALIDATION_ERROR",
                        message = "입력값 검증에 실패했습니다",
                        details = details,
                    ),
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다"))
    }
}
