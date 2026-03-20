package com.pianocompanion.api.domain.user.repository

import com.pianocompanion.api.domain.user.entity.AuthProvider
import com.pianocompanion.api.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmailAndDeletedAtIsNull(email: String): User?

    fun findByProviderAndProviderIdAndDeletedAtIsNull(provider: AuthProvider, providerId: String): User?

    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean
}
