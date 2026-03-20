package com.pianocompanion.api.domain.goal.repository

import com.pianocompanion.api.domain.goal.entity.Goal
import com.pianocompanion.api.domain.goal.entity.GoalType
import org.springframework.data.jpa.repository.JpaRepository

interface GoalRepository : JpaRepository<Goal, Long> {

    fun findByUserIdAndTypeAndIsActiveTrue(userId: Long, type: GoalType): Goal?

    fun findByUserIdAndIsActiveTrue(userId: Long): List<Goal>

    fun findByUserIdAndTypeInAndIsActiveTrue(userId: Long, types: List<GoalType>): List<Goal>
}
