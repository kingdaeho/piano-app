package com.pianocompanion.api.domain.auth

import com.pianocompanion.api.domain.auth.dto.LoginRequest
import com.pianocompanion.api.domain.auth.dto.SignupRequest
import com.pianocompanion.api.domain.auth.service.AuthService
import com.pianocompanion.api.domain.auth.service.JwtTokenProvider
import com.pianocompanion.api.domain.auth.service.RefreshTokenInfo
import com.pianocompanion.api.domain.auth.service.RefreshTokenService
import com.pianocompanion.api.domain.user.entity.AuthProvider
import com.pianocompanion.api.domain.user.entity.ExperienceLevel
import com.pianocompanion.api.domain.user.entity.User
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.global.exception.AuthenticationException
import com.pianocompanion.api.global.exception.DuplicateException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class AuthServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val jwtTokenProvider = mockk<JwtTokenProvider>()
    val refreshTokenService = mockk<RefreshTokenService>(relaxed = true)

    val authService = AuthService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenService)

    given("회원가입") {
        val request = SignupRequest(
            email = "test@example.com",
            password = "password123",
            name = "테스트",
            experienceLevel = ExperienceLevel.BEGINNER,
        )

        `when`("신규 이메일로 가입하면") {
            every { userRepository.existsByEmailAndDeletedAtIsNull(request.email) } returns false
            every { passwordEncoder.encode(request.password) } returns "encoded_password"
            every { userRepository.save(any()) } answers {
                val user = firstArg<User>()
                user
            }
            every { jwtTokenProvider.generateAccessToken(any(), any()) } returns "access_token"
            every { jwtTokenProvider.generateRefreshToken(any()) } returns RefreshTokenInfo(
                token = "refresh_token",
                tokenId = "token_id",
                expiryMillis = 1209600000,
            )

            val result = authService.signup(request)

            then("인증 응답을 반환한다") {
                result.accessToken shouldBe "access_token"
                result.refreshToken shouldBe "refresh_token"
                result.user.email shouldBe "test@example.com"
                result.user.name shouldBe "테스트"
            }
        }

        `when`("이미 존재하는 이메일로 가입하면") {
            every { userRepository.existsByEmailAndDeletedAtIsNull(request.email) } returns true

            then("DuplicateException이 발생한다") {
                shouldThrow<DuplicateException> {
                    authService.signup(request)
                }
            }
        }
    }

    given("로그인") {
        val request = LoginRequest(email = "test@example.com", password = "password123")
        val user = User(
            email = "test@example.com",
            passwordHash = "encoded_password",
            name = "테스트",
            provider = AuthProvider.LOCAL,
        )

        `when`("올바른 자격증명으로 로그인하면") {
            every { userRepository.findByEmailAndDeletedAtIsNull(request.email) } returns user
            every { passwordEncoder.matches(request.password, "encoded_password") } returns true
            every { jwtTokenProvider.generateAccessToken(any(), any()) } returns "access_token"
            every { jwtTokenProvider.generateRefreshToken(any()) } returns RefreshTokenInfo(
                token = "refresh_token",
                tokenId = "token_id",
                expiryMillis = 1209600000,
            )

            val result = authService.login(request)

            then("인증 응답을 반환한다") {
                result.accessToken shouldNotBe null
                result.user.email shouldBe "test@example.com"
            }
        }

        `when`("존재하지 않는 이메일로 로그인하면") {
            every { userRepository.findByEmailAndDeletedAtIsNull(request.email) } returns null

            then("AuthenticationException이 발생한다") {
                shouldThrow<AuthenticationException> {
                    authService.login(request)
                }
            }
        }

        `when`("잘못된 비밀번호로 로그인하면") {
            every { userRepository.findByEmailAndDeletedAtIsNull(request.email) } returns user
            every { passwordEncoder.matches(request.password, "encoded_password") } returns false

            then("AuthenticationException이 발생한다") {
                shouldThrow<AuthenticationException> {
                    authService.login(request)
                }
            }
        }
    }

    given("로그아웃") {
        `when`("리프레시 토큰과 함께 로그아웃하면") {
            every { jwtTokenProvider.parseRefreshToken(any()) } returns com.pianocompanion.api.domain.auth.service.RefreshTokenClaims(
                userId = 1L,
                tokenId = "token_id",
            )

            authService.logout(1L, "refresh_token")

            then("리프레시 토큰이 무효화된다") {
                verify { refreshTokenService.invalidate(1L, "token_id") }
            }
        }
    }
})
