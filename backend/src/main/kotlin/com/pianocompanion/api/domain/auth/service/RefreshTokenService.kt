package com.pianocompanion.api.domain.auth.service

import com.pianocompanion.api.global.common.getLogger
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate,
) {

    private val logger = getLogger()

    fun saveRefreshToken(userId: Long, tokenId: String, expiryMillis: Long) {
        val key = buildKey(userId, tokenId)
        redisTemplate.opsForValue().set(key, tokenId, Duration.ofMillis(expiryMillis))
        logger.debug("Refresh token saved: userId={}, tokenId={}", userId, tokenId)
    }

    fun isValid(userId: Long, tokenId: String): Boolean {
        val key = buildKey(userId, tokenId)
        return redisTemplate.hasKey(key)
    }

    fun invalidate(userId: Long, tokenId: String) {
        val key = buildKey(userId, tokenId)
        redisTemplate.delete(key)
        logger.debug("Refresh token invalidated: userId={}, tokenId={}", userId, tokenId)
    }

    fun invalidateAllForUser(userId: Long) {
        val pattern = "$KEY_PREFIX$userId:*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
            logger.warn("All refresh tokens invalidated for userId={}, count={}", userId, keys.size)
        }
    }

    private fun buildKey(userId: Long, tokenId: String): String =
        "$KEY_PREFIX$userId:$tokenId"

    companion object {
        private const val KEY_PREFIX = "refresh_token:"
    }
}
