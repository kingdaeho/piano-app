"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft, ChevronRight, Settings } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useWeeklyStats, useGoals } from "@/api/hooks";
import { formatDurationHM } from "@/lib/format";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { getDayOfWeek } from "@/lib/format";

type Tab = "weekly" | "goals";

export default function StatsPage() {
  const [tab, setTab] = useState<Tab>("weekly");
  const { data: weeklyStats } = useWeeklyStats();
  const { data: goals } = useGoals();
  const router = useRouter();

  const tabs: { key: Tab; label: string }[] = [
    { key: "weekly", label: "주간" },
    { key: "goals", label: "목표" },
  ];

  return (
    <div className="space-y-4 px-5 py-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">통계</h1>
        <button onClick={() => router.push("/goals")}>
          <Settings className="h-5 w-5 text-[#616161]" />
        </button>
      </div>

      {/* Tab Switcher */}
      <div className="flex gap-2">
        {tabs.map((t) => (
          <Button
            key={t.key}
            variant={tab === t.key ? "default" : "outline"}
            size="sm"
            onClick={() => setTab(t.key)}
            className={tab === t.key ? "bg-[#3F51B5]" : ""}
          >
            {t.label}
          </Button>
        ))}
      </div>

      {tab === "weekly" && weeklyStats && (
        <WeeklyView stats={weeklyStats} />
      )}
      {tab === "goals" && goals && <GoalsView goals={goals} />}
    </div>
  );
}

function WeeklyView({
  stats,
}: {
  stats: NonNullable<ReturnType<typeof useWeeklyStats>["data"]>;
}) {
  const chartData = stats.dailyStats.map((d) => ({
    day: getDayOfWeek(d.date),
    minutes: Math.round(d.durationSeconds / 60),
  }));

  return (
    <div className="space-y-4">
      {/* Week Navigation */}
      <div className="flex items-center justify-center gap-4 text-sm">
        <ChevronLeft className="h-5 w-5 cursor-pointer text-[#616161]" />
        <span className="font-medium">
          {stats.weekStart} ~ {stats.weekEnd}
        </span>
        <ChevronRight className="h-5 w-5 cursor-pointer text-[#616161]" />
      </div>

      {/* Summary */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">이번 주 요약</h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-[#9E9E9E]">총 연습 시간</p>
              <p className="text-xl font-bold">
                {formatDurationHM(stats.totalDurationSeconds)}
              </p>
              {stats.changePercent !== 0 && (
                <p
                  className={`text-xs ${stats.changePercent > 0 ? "text-[#4CAF50]" : "text-[#F44336]"}`}
                >
                  {stats.changePercent > 0 ? "+" : ""}
                  {stats.changePercent.toFixed(1)}% vs 지난주
                </p>
              )}
            </div>
            <div>
              <p className="text-xs text-[#9E9E9E]">연습 일수</p>
              <p className="text-xl font-bold">{stats.practiceDays}일</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Daily Chart */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">일별 연습 시간</h2>
          <div className="h-[180px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <XAxis
                  dataKey="day"
                  tick={{ fontSize: 12, fill: "#616161" }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#9E9E9E" }}
                  axisLine={false}
                  tickLine={false}
                  width={30}
                />
                <Bar dataKey="minutes" radius={[4, 4, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell
                      key={index}
                      fill={entry.minutes > 0 ? "#3F51B5" : "#E0E0E0"}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Piece Stats */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">곡별 연습 비율</h2>
          <div className="space-y-3">
            {stats.pieceStats.map((piece) => (
              <div key={piece.pieceId} className="space-y-1">
                <div className="flex items-center justify-between text-sm">
                  <span>{piece.title}</span>
                  <span className="text-[#616161]">{piece.percent}%</span>
                </div>
                <Progress value={piece.percent} className="h-2" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function GoalsView({
  goals,
}: {
  goals: NonNullable<ReturnType<typeof useGoals>["data"]>;
}) {
  return (
    <div className="space-y-4">
      {/* Daily Goal */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">일일 목표</h2>
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span>하루 목표</span>
              <span className="font-medium">
                {goals.daily.targetMinutes}분
              </span>
            </div>
            <Progress value={goals.daily.percent} className="h-3" />
            <p className="text-sm text-[#616161]">
              {goals.daily.achievedMinutes}/{goals.daily.targetMinutes}분 (
              {goals.daily.percent}%)
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Weekly Goal */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">주간 목표</h2>
          <div className="space-y-3">
            <div>
              <div className="flex items-center justify-between text-sm">
                <span>연습 일수</span>
                <span>
                  {goals.weekly.achievedDays}/{goals.weekly.targetDays}일
                </span>
              </div>
              <Progress
                value={
                  (goals.weekly.achievedDays / goals.weekly.targetDays) * 100
                }
                className="mt-1 h-2"
              />
            </div>
            <div>
              <div className="flex items-center justify-between text-sm">
                <span>연습 시간</span>
                <span>
                  {goals.weekly.achievedMinutes}/{goals.weekly.targetMinutes}분
                </span>
              </div>
              <Progress
                value={
                  (goals.weekly.achievedMinutes /
                    goals.weekly.targetMinutes) *
                  100
                }
                className="mt-1 h-2"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Streak */}
      <Card>
        <CardContent className="pt-4 text-center">
          <p className="text-3xl">🔥</p>
          <p className="text-2xl font-bold text-[#FF6D00]">
            {goals.streak.currentDays}일 연속 달성!
          </p>
          <p className="mt-1 text-sm text-[#616161]">
            최장 스트릭: {goals.streak.longestDays}일
          </p>
        </CardContent>
      </Card>

      {/* Piece Goals */}
      {goals.pieceGoals.length > 0 && (
        <Card>
          <CardContent className="pt-4">
            <h2 className="mb-3 font-medium">곡 완성 목표</h2>
            <div className="space-y-4">
              {goals.pieceGoals.map((g) => (
                <div key={g.id} className="space-y-1">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium">{g.piece.title}</span>
                    <span className="text-[#616161]">
                      D-{g.daysRemaining}
                    </span>
                  </div>
                  <p className="text-xs text-[#9E9E9E]">
                    목표일: {g.targetDate}
                  </p>
                  <div className="flex items-center gap-2">
                    <Progress
                      value={g.currentProgressPercent}
                      className="h-2 flex-1"
                    />
                    <span className="text-xs">{g.currentProgressPercent}%</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
