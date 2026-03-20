package com.pianocompanion.api.global.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val meta: PageMeta? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun <T> ok(data: T, meta: PageMeta): ApiResponse<T> =
            ApiResponse(success = true, data = data, meta = meta)

        fun error(code: String, message: String): ApiResponse<Nothing> =
            ApiResponse(success = false, error = ErrorDetail(code = code, message = message))
    }
}

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<String>? = null,
)

data class PageMeta(
    val total: Long,
    val page: Int,
    val size: Int,
)
