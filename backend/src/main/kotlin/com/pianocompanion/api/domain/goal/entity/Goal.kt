package com.pianocompanion.api.domain.goal.entity

import com.pianocompanion.api.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "goals")
class Goal(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "piece_id")
    val pieceId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val type: GoalType,

    @Column(name = "target_value", nullable = false)
    var targetValue: Int,

    @Column(name = "target_date")
    var targetDate: LocalDate? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : BaseEntity()

enum class GoalType {
    DAILY_TIME,
    WEEKLY_DAYS,
    WEEKLY_TIME,
    PIECE_COMPLETION,
}
