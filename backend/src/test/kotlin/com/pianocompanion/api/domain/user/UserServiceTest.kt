package com.pianocompanion.api.domain.user

import com.pianocompanion.api.domain.user.dto.UpdateProfileRequest
import com.pianocompanion.api.domain.user.entity.AuthProvider
import com.pianocompanion.api.domain.user.entity.ExperienceLevel
import com.pianocompanion.api.domain.user.entity.User
import com.pianocompanion.api.domain.user.repository.UserRepository
import com.pianocompanion.api.domain.user.service.UserService
import com.pianocompanion.api.global.exception.EntityNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class UserServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val userService = UserService(userRepository)

    val userId = 1L
    val user = User(
        email = "test@example.com",
        passwordHash = "hashed",
        name = "테스트",
        experienceLevel = ExperienceLevel.BEGINNER,
        provider = AuthProvider.LOCAL,
        dailyGoalMinutes = 60,
        weeklyGoalDays = 5,
        weeklyGoalMinutes = 300,
    )

    given("프로필 조회") {
        `when`("존재하는 사용자를 조회하면") {
            every { userRepository.findById(userId) } returns Optional.of(user)

            val result = userService.getProfile(userId)

            then("사용자 정보를 반환한다") {
                result.email shouldBe "test@example.com"
                result.name shouldBe "테스트"
                result.experienceLevel shouldBe ExperienceLevel.BEGINNER
                result.dailyGoalMinutes shouldBe 60
            }
        }

        `when`("존재하지 않는 사용자를 조회하면") {
            every { userRepository.findById(99L) } returns Optional.empty()

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    userService.getProfile(99L)
                }
            }
        }
    }

    given("프로필 수정") {
        `when`("이름과 경험 레벨을 수정하면") {
            every { userRepository.findById(userId) } returns Optional.of(user)

            val request = UpdateProfileRequest(
                name = "수정된이름",
                experienceLevel = ExperienceLevel.LESSON_STUDENT,
            )
            val result = userService.updateProfile(userId, request)

            then("수정된 정보를 반환한다") {
                result.name shouldBe "수정된이름"
                result.experienceLevel shouldBe ExperienceLevel.LESSON_STUDENT
            }
        }

        `when`("목표 설정을 수정하면") {
            val freshUser = User(
                email = "test2@example.com",
                name = "테스트2",
                provider = AuthProvider.LOCAL,
            )
            every { userRepository.findById(2L) } returns Optional.of(freshUser)

            val request = UpdateProfileRequest(
                dailyGoalMinutes = 90,
                weeklyGoalDays = 6,
                weeklyGoalMinutes = 450,
            )
            val result = userService.updateProfile(2L, request)

            then("목표가 수정된다") {
                result.dailyGoalMinutes shouldBe 90
                result.weeklyGoalDays shouldBe 6
                result.weeklyGoalMinutes shouldBe 450
            }
        }

        `when`("존재하지 않는 사용자 프로필을 수정하면") {
            every { userRepository.findById(99L) } returns Optional.empty()

            then("EntityNotFoundException이 발생한다") {
                shouldThrow<EntityNotFoundException> {
                    userService.updateProfile(99L, UpdateProfileRequest(name = "새이름"))
                }
            }
        }
    }
})
