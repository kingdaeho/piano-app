package com.pianocompanion.api.global.exception

import org.springframework.http.HttpStatus

sealed class BusinessException(
    message: String,
    val errorCode: String,
    val httpStatus: HttpStatus,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class EntityNotFoundException(
    entityName: String,
    id: Any,
) : BusinessException(
    message = "${entityName}을(를) 찾을 수 없습니다: $id",
    errorCode = "${entityName.uppercase()}_NOT_FOUND",
    httpStatus = HttpStatus.NOT_FOUND,
)

class DuplicateException(
    message: String,
    errorCode: String = "DUPLICATE",
) : BusinessException(
    message = message,
    errorCode = errorCode,
    httpStatus = HttpStatus.CONFLICT,
)

class AuthenticationException(
    message: String,
    errorCode: String = "AUTHENTICATION_FAILED",
) : BusinessException(
    message = message,
    errorCode = errorCode,
    httpStatus = HttpStatus.UNAUTHORIZED,
)

class ForbiddenException(
    message: String,
    errorCode: String = "FORBIDDEN",
) : BusinessException(
    message = message,
    errorCode = errorCode,
    httpStatus = HttpStatus.FORBIDDEN,
)

class InvalidRequestException(
    message: String,
    errorCode: String = "INVALID_REQUEST",
) : BusinessException(
    message = message,
    errorCode = errorCode,
    httpStatus = HttpStatus.BAD_REQUEST,
)
