package com.pianocompanion.api.domain.user.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, length = 255)
    val email: String,

    @Column(name = "password_hash", length = 255)
    var passwordHash: String? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false, length = 20)
    var experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,

    @Column(name = "daily_goal_minutes", nullable = false)
    var dailyGoalMinutes: Int = DEFAULT_DAILY_GOAL_MINUTES,

    @Column(name = "weekly_goal_days", nullable = false)
    var weeklyGoalDays: Int = DEFAULT_WEEKLY_GOAL_DAYS,

    @Column(name = "weekly_goal_minutes", nullable = false)
    var weeklyGoalMinutes: Int = DEFAULT_WEEKLY_GOAL_MINUTES,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column(name = "provider_id", length = 255)
    val providerId: String? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
) : BaseEntity() {

    fun updateProfile(
        name: String? = null,
        profileImageUrl: String? = null,
        experienceLevel: ExperienceLevel? = null,
    ) {
        name?.let { this.name = it }
        profileImageUrl?.let { this.profileImageUrl = it }
        experienceLevel?.let { this.experienceLevel = it }
    }

    fun updateGoals(
        dailyGoalMinutes: Int? = null,
        weeklyGoalDays: Int? = null,
        weeklyGoalMinutes: Int? = null,
    ) {
        dailyGoalMinutes?.let { this.dailyGoalMinutes = it }
        weeklyGoalDays?.let { this.weeklyGoalDays = it }
        weeklyGoalMinutes?.let { this.weeklyGoalMinutes = it }
    }

    companion object {
        const val DEFAULT_DAILY_GOAL_MINUTES = 60
        const val DEFAULT_WEEKLY_GOAL_DAYS = 5
        const val DEFAULT_WEEKLY_GOAL_MINUTES = 300
    }
}

enum class ExperienceLevel {
    BEGINNER,
    LESSON_STUDENT,
    RETURNER,
}

enum class AuthProvider {
    LOCAL,
    GOOGLE,
    APPLE,
}
