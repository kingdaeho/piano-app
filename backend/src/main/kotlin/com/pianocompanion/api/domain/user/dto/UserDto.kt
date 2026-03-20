package com.pianocompanion.api.domain.user.dto

import com.pianocompanion.api.domain.user.entity.ExperienceLevel
import com.pianocompanion.api.domain.user.entity.User
import jakarta.validation.constraints.Size

data class UserView(
    val id: Long,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
    val experienceLevel: ExperienceLevel,
    val dailyGoalMinutes: Int,
    val weeklyGoalDays: Int,
    val weeklyGoalMinutes: Int,
) {
    companion object {
        fun from(user: User): UserView = UserView(
            id = user.id,
            email = user.email,
            name = user.name,
            profileImageUrl = user.profileImageUrl,
            experienceLevel = user.experienceLevel,
            dailyGoalMinutes = user.dailyGoalMinutes,
            weeklyGoalDays = user.weeklyGoalDays,
            weeklyGoalMinutes = user.weeklyGoalMinutes,
        )
    }
}

data class UpdateProfileRequest(
    @field:Size(max = 100, message = "이름은 100자 이하여야 합니다")
    val name: String? = null,

    val profileImageUrl: String? = null,

    val experienceLevel: ExperienceLevel? = null,

    val dailyGoalMinutes: Int? = null,

    val weeklyGoalDays: Int? = null,

    val weeklyGoalMinutes: Int? = null,
)
