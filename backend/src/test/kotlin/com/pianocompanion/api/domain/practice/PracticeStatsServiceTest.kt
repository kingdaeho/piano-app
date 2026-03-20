package com.pianocompanion.api.domain.practice

import com.pianocompanion.api.domain.piece.entity.Piece
import com.pianocompanion.api.domain.piece.entity.PieceStatus
import com.pianocompanion.api.domain.piece.repository.PieceRepository
import com.pianocompanion.api.domain.practice.entity.PracticeSession
import com.pianocompanion.api.domain.practice.repository.PracticeSessionPieceRepository
import com.pianocompanion.api.domain.practice.repository.PracticeSessionRepository
import com.pianocompanion.api.domain.practice.service.PracticeStatsService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId

class PracticeStatsServiceTest : BehaviorSpec({
    val practiceSessionRepository = mockk<PracticeSessionRepository>()
    val practiceSessionPieceRepository = mockk<PracticeSessionPieceRepository>()
    val pieceRepository = mockk<PieceRepository>()

    val service = PracticeStatsService(
        practiceSessionRepository,
        practiceSessionPieceRepository,
        pieceRepository,
    )
    val userId = 1L
    val zoneId = ZoneId.of("Asia/Seoul")

    given("주간 통계 조회") {
        `when`("연습 기록이 없는 경우") {
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every {
                practiceSessionPieceRepository.sumDurationByPieceInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()
            every {
                practiceSessionRepository.sumDurationInRange(eq(userId), any(), any())
            } returns 0

            val result = service.getWeeklyStats(userId, LocalDate.of(2026, 3, 19), zoneId)

            then("모든 값이 0이다") {
                result.totalDurationSeconds shouldBe 0
                result.practiceDays shouldBe 0
                result.dailyStats.size shouldBe 7
                result.dailyStats.all { it.durationSeconds == 0 } shouldBe true
                result.pieceStats shouldBe emptyList()
                result.changePercent shouldBe 0.0
            }
        }

        `when`("이번 주 연습 기록이 있는 경우") {
            val date = LocalDate.of(2026, 3, 19) // 목요일
            val mondayStart = LocalDate.of(2026, 3, 16) // 월요일
            val sessionTime = mondayStart.atTime(18, 0).atZone(zoneId).toInstant()

            val session = PracticeSession(
                userId = userId,
                startedAt = sessionTime,
                totalDurationSeconds = 3600,
            ).apply { endedAt = sessionTime.plusSeconds(3600) }

            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns listOf(session)
            every {
                practiceSessionPieceRepository.sumDurationByPieceInRange(eq(userId), any(), any())
            } returns listOf(arrayOf<Any>(1L, 3600))
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns listOf(
                Piece(userId = userId, title = "테스트곡", status = PieceStatus.PRACTICING),
            )
            every {
                practiceSessionRepository.sumDurationInRange(eq(userId), any(), any())
            } returns 0

            val result = service.getWeeklyStats(userId, date, zoneId)

            then("통계가 올바르게 집계된다") {
                result.totalDurationSeconds shouldBe 3600
                result.practiceDays shouldBe 1
                result.weekStart shouldBe mondayStart
            }
        }

        `when`("전주 대비 변화가 있는 경우") {
            val date = LocalDate.of(2026, 3, 19)
            val sessionTime = date.atTime(18, 0).atZone(zoneId).toInstant()

            val session = PracticeSession(
                userId = userId,
                startedAt = sessionTime,
                totalDurationSeconds = 7200,
            ).apply { endedAt = sessionTime.plusSeconds(7200) }

            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns listOf(session)
            every {
                practiceSessionPieceRepository.sumDurationByPieceInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()
            // 전주 3600초 연습
            every {
                practiceSessionRepository.sumDurationInRange(eq(userId), any(), any())
            } returns 3600

            val result = service.getWeeklyStats(userId, date, zoneId)

            then("전주 대비 변화율이 계산된다") {
                result.previousWeekDurationSeconds shouldBe 3600
                result.changePercent shouldBeGreaterThan 0.0
            }
        }

        `when`("곡별 통계가 여러 곡에 분산된 경우") {
            val date = LocalDate.of(2026, 3, 19)
            val sessionTime = date.atTime(18, 0).atZone(zoneId).toInstant()

            val session = PracticeSession(
                userId = userId,
                startedAt = sessionTime,
                totalDurationSeconds = 5400,
            ).apply { endedAt = sessionTime.plusSeconds(5400) }

            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns listOf(session)
            every {
                practiceSessionPieceRepository.sumDurationByPieceInRange(eq(userId), any(), any())
            } returns listOf(
                arrayOf<Any>(1L, 3600),
                arrayOf<Any>(2L, 1800),
            )
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns listOf(
                Piece(userId = userId, title = "체르니", status = PieceStatus.PRACTICING),
                Piece(userId = userId, title = "바이엘", status = PieceStatus.PRACTICING),
            )
            every {
                practiceSessionRepository.sumDurationInRange(eq(userId), any(), any())
            } returns 0

            val result = service.getWeeklyStats(userId, date, zoneId)

            then("곡별 통계가 연습 시간 내림차순으로 정렬된다") {
                result.pieceStats.size shouldBe 2
                result.pieceStats.first().durationSeconds shouldBe 3600
                result.pieceStats.last().durationSeconds shouldBe 1800
            }

            then("비율이 올바르게 계산된다") {
                result.pieceStats.first().percent shouldBeGreaterThan 0.0
            }
        }

        `when`("주 시작/종료 날짜가 올바르게 계산되는지 (수요일 기준)") {
            every {
                practiceSessionRepository.findCompletedSessionsInRange(eq(userId), any(), any())
            } returns emptyList()
            every {
                practiceSessionPieceRepository.sumDurationByPieceInRange(eq(userId), any(), any())
            } returns emptyList()
            every { pieceRepository.findAllByUserIdAndDeletedAtIsNull(userId) } returns emptyList()
            every {
                practiceSessionRepository.sumDurationInRange(eq(userId), any(), any())
            } returns 0

            val wednesday = LocalDate.of(2026, 3, 18)
            val result = service.getWeeklyStats(userId, wednesday, zoneId)

            then("월요일부터 일요일까지이다") {
                result.weekStart shouldBe LocalDate.of(2026, 3, 16)
                result.weekEnd shouldBe LocalDate.of(2026, 3, 22)
            }
        }
    }
})
