package com.pianocompanion.api.domain.auth

import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.global.config.JwtProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class JwtTokenProviderTest : FunSpec({
    val properties = JwtProperties(
        secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-test",
        accessTokenExpiry = 1800000,
        refreshTokenExpiry = 1209600000,
    )
    val jwtTokenProvider = JwtTokenProvider(properties)

    test("Access Token 생성 및 검증") {
        val token = jwtTokenProvider.generateAccessToken(1L, "test@example.com")
        val principal = jwtTokenProvider.validateAccessToken(token)

        principal.shouldNotBeNull()
        principal.userId shouldBe 1L
        principal.email shouldBe "test@example.com"
    }

    test("Refresh Token 생성 및 파싱") {
        val refreshTokenInfo = jwtTokenProvider.generateRefreshToken(1L)
        val claims = jwtTokenProvider.parseRefreshToken(refreshTokenInfo.token)

        claims.shouldNotBeNull()
        claims.userId shouldBe 1L
        claims.tokenId shouldBe refreshTokenInfo.tokenId
    }

    test("잘못된 토큰은 null을 반환한다") {
        val result = jwtTokenProvider.validateAccessToken("invalid.token.here")
        result.shouldBeNull()
    }

    test("Refresh Token을 Access Token으로 검증하면 null을 반환한다") {
        val refreshTokenInfo = jwtTokenProvider.generateRefreshToken(1L)
        val result = jwtTokenProvider.validateAccessToken(refreshTokenInfo.token)
        result.shouldBeNull()
    }

    test("Access Token을 Refresh Token으로 파싱하면 null을 반환한다") {
        val accessToken = jwtTokenProvider.generateAccessToken(1L, "test@example.com")
        val result = jwtTokenProvider.parseRefreshToken(accessToken)
        result.shouldBeNull()
    }
})
