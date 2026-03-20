package com.pianocompanion.api.domain.auth.service

import com.pianocompanion.api.domain.auth.dto.AuthResponse
import com.pianocompanion.api.domain.auth.dto.LoginRequest
import com.pianocompanion.api.domain.auth.dto.RefreshRequest
import com.pianocompanion.api.domain.auth.dto.SignupRequest
import com.pianocompanion.api.domain.auth.dto.TokenResponse
import com.pianocompanion.api.domain.user.dto.UserView
import com.pianocompanion.api.domain.user.entity.AuthProvider
import com.pianocompanion.api.domain.user.entity.User
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.AuthenticationException
import com.pianocompanion.api.global.exception.DuplicateException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
) {

    private val logger = getLogger()

    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email)) {
            throw DuplicateException("이미 사용 중인 이메일입니다", "EMAIL_DUPLICATE")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            experienceLevel = request.experienceLevel,
            provider = AuthProvider.LOCAL,
        )
        val savedUser = userRepository.save(user)
        logger.info("New user signed up: userId={}, email={}", savedUser.id, savedUser.email)

        return generateAuthResponse(savedUser)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmailAndDeletedAtIsNull(request.email)
            ?: throw AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다")

        val passwordHash = user.passwordHash
            ?: throw AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다")

        if (!passwordEncoder.matches(request.password, passwordHash)) {
            throw AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다")
        }

        logger.info("User logged in: userId={}", user.id)
        return generateAuthResponse(user)
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val claims = jwtTokenProvider.parseRefreshToken(request.refreshToken)
            ?: throw AuthenticationException("유효하지 않은 리프레시 토큰입니다", "INVALID_REFRESH_TOKEN")

        if (!refreshTokenService.isValid(claims.userId, claims.tokenId)) {
            refreshTokenService.invalidateAllForUser(claims.userId)
            throw AuthenticationException("만료되었거나 이미 사용된 리프레시 토큰입니다", "REFRESH_TOKEN_REUSE")
        }

        val user = userRepository.findById(claims.userId)
            .orElseThrow { AuthenticationException("사용자를 찾을 수 없습니다") }

        refreshTokenService.invalidate(claims.userId, claims.tokenId)

        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.email)
        val refreshTokenInfo = jwtTokenProvider.generateRefreshToken(user.id)
        refreshTokenService.saveRefreshToken(user.id, refreshTokenInfo.tokenId, refreshTokenInfo.expiryMillis)

        logger.debug("Token refreshed for userId={}", user.id)
        return TokenResponse(accessToken = accessToken, refreshToken = refreshTokenInfo.token)
    }

    fun logout(userId: Long, refreshToken: String?) {
        if (refreshToken != null) {
            val claims = jwtTokenProvider.parseRefreshToken(refreshToken)
            if (claims != null) {
                refreshTokenService.invalidate(claims.userId, claims.tokenId)
            }
        }
        logger.info("User logged out: userId={}", userId)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.email)
        val refreshTokenInfo = jwtTokenProvider.generateRefreshToken(user.id)
        refreshTokenService.saveRefreshToken(user.id, refreshTokenInfo.tokenId, refreshTokenInfo.expiryMillis)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenInfo.token,
            user = UserView.from(user),
        )
    }
}
