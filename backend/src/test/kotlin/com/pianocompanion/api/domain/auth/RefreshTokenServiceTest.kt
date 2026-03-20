package com.pianocompanion.api.domain.auth

import com.pianocompanion.api.domain.auth.service.RefreshTokenService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class RefreshTokenServiceTest : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    val redisTemplate = mockk<StringRedisTemplate>(relaxed = true)
    val valueOps = mockk<ValueOperations<String, String>>(relaxed = true)

    every { redisTemplate.opsForValue() } returns valueOps

    val service = RefreshTokenService(redisTemplate)

    test("saveRefreshToken은 Redis에 키를 저장한다") {
        service.saveRefreshToken(1L, "token-123", 86400000)

        verify {
            valueOps.set("refresh_token:1:token-123", "token-123", Duration.ofMillis(86400000))
        }
    }

    test("isValid는 키가 존재하면 true를 반환한다") {
        every { redisTemplate.hasKey("refresh_token:1:token-123") } returns true

        val result = service.isValid(1L, "token-123")

        result shouldBe true
    }

    test("isValid는 키가 없으면 false를 반환한다") {
        every { redisTemplate.hasKey("refresh_token:1:expired-token") } returns false

        val result = service.isValid(1L, "expired-token")

        result shouldBe false
    }

    test("invalidate는 키를 삭제한다") {
        service.invalidate(1L, "token-123")

        verify { redisTemplate.delete("refresh_token:1:token-123") }
    }

    test("invalidateAllForUser는 해당 사용자의 모든 토큰을 삭제한다") {
        val keys = setOf("refresh_token:1:token-1", "refresh_token:1:token-2")
        every { redisTemplate.keys("refresh_token:1:*") } returns keys

        service.invalidateAllForUser(1L)

        verify { redisTemplate.delete(keys) }
    }

    test("invalidateAllForUser는 키가 없으면 삭제하지 않는다") {
        every { redisTemplate.keys("refresh_token:2:*") } returns emptySet()

        service.invalidateAllForUser(2L)

        verify(exactly = 0) { redisTemplate.delete(any<Collection<String>>()) }
    }
})
