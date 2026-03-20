package com.pianocompanion.api.domain.user.service

import com.pianocompanion.api.domain.user.dto.UpdateProfileRequest
import com.pianocompanion.api.domain.user.dto.UserView
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.global.common.getLogger
import com.pianocompanion.api.global.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger = getLogger()

    @Transactional(readOnly = true)
    fun getProfile(userId: Long): UserView {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }
        return UserView.from(user)
    }

    @Transactional
    fun updateProfile(userId: Long, request: UpdateProfileRequest): UserView {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User", userId) }

        user.updateProfile(
            name = request.name,
            profileImageUrl = request.profileImageUrl,
            experienceLevel = request.experienceLevel,
        )
        user.updateGoals(
            dailyGoalMinutes = request.dailyGoalMinutes,
            weeklyGoalDays = request.weeklyGoalDays,
            weeklyGoalMinutes = request.weeklyGoalMinutes,
        )

        logger.debug("User profile updated: userId={}", userId)
        return UserView.from(user)
    }
}
