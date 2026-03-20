package com.pianocompanion.api.domain.auth.service

import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.config.JwtProperties
import com.pianocompanion.api.global.security.UserPrincipal
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {

    private val logger = getLogger()

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.accessTokenExpiry)
        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(userId: Long): RefreshTokenInfo {
        val tokenId = UUID.randomUUID().toString()
        val now = Date()
        val expiry = Date(now.time + jwtProperties.refreshTokenExpiry)
        val token = Jwts.builder()
            .subject(userId.toString())
            .id(tokenId)
            .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
        return RefreshTokenInfo(
            token = token,
            tokenId = tokenId,
            expiryMillis = jwtProperties.refreshTokenExpiry,
        )
    }

    fun validateAccessToken(token: String): UserPrincipal? {
        return runCatching {
            val claims = parseToken(token)
            val type = claims[CLAIM_TYPE] as? String
            if (type != TOKEN_TYPE_ACCESS) return null

            val userId = claims.subject.toLong()
            val email = claims[CLAIM_EMAIL] as String
            UserPrincipal(userId = userId, email = email)
        }.onFailure { e ->
            when (e) {
                is ExpiredJwtException -> logger.debug("Access token expired")
                is JwtException -> logger.warn("Invalid access token: {}", e.message)
                else -> logger.error("Token validation error: {}", e.message)
            }
        }.getOrNull()
    }

    fun parseRefreshToken(token: String): RefreshTokenClaims? {
        return runCatching {
            val claims = parseToken(token)
            val type = claims[CLAIM_TYPE] as? String
            if (type != TOKEN_TYPE_REFRESH) return null

            RefreshTokenClaims(
                userId = claims.subject.toLong(),
                tokenId = claims.id,
            )
        }.onFailure { e ->
            logger.warn("Invalid refresh token: {}", e.message)
        }.getOrNull()
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_TYPE = "type"
        private const val TOKEN_TYPE_ACCESS = "access"
        private const val TOKEN_TYPE_REFRESH = "refresh"
    }
}

data class RefreshTokenInfo(
    val token: String,
    val tokenId: String,
    val expiryMillis: Long,
)

data class RefreshTokenClaims(
    val userId: Long,
    val tokenId: String,
)
