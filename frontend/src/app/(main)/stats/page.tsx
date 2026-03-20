"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft, ChevronRight, Settings, Clock, Calendar, TrendingUp, Target, Flame } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
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
    <div className="space-y-5 px-5 pb-8 pt-7">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold tracking-tight text-foreground">통계</h1>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => router.push("/goals")}
        >
          <Settings className="h-5 w-5 text-muted-foreground" />
        </Button>
      </div>

      {/* Tab Switcher */}
      <div className="flex gap-2">
        {tabs.map((t) => (
          <Button
            key={t.key}
            variant={tab === t.key ? "default" : "outline"}
            size="sm"
            onClick={() => setTab(t.key)}
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
      <div className="flex items-center justify-center gap-2 text-sm">
        <Button variant="ghost" size="icon" className="h-11 w-11">
          <ChevronLeft className="h-5 w-5 text-muted-foreground" />
        </Button>
        <span className="font-medium text-foreground">
          {stats.weekStart} ~ {stats.weekEnd}
        </span>
        <Button variant="ghost" size="icon" className="h-11 w-11">
          <ChevronRight className="h-5 w-5 text-muted-foreground" />
        </Button>
      </div>

      {/* Summary */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-3 font-semibold text-foreground">이번 주 요약</h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-start gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                <Clock className="h-4 w-4 text-primary" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">총 연습 시간</p>
                <p className="text-xl font-bold text-foreground">
                  {formatDurationHM(stats.totalDurationSeconds)}
                </p>
                {stats.changePercent !== 0 && (
                  <p
                    className={`flex items-center gap-0.5 text-xs ${stats.changePercent > 0 ? "text-success" : "text-destructive"}`}
                  >
                    <TrendingUp className="h-3 w-3" />
                    {stats.changePercent > 0 ? "+" : ""}
                    {stats.changePercent.toFixed(1)}% vs 지난주
                  </p>
                )}
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                <Calendar className="h-4 w-4 text-primary" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">연습 일수</p>
                <p className="text-xl font-bold text-foreground">{stats.practiceDays}일</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Daily Chart */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-3 font-semibold text-foreground">일별 연습 시간</h2>
          <div className="h-[180px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <XAxis
                  dataKey="day"
                  tick={{ fontSize: 12 }}
                  axisLine={false}
                  tickLine={false}
                  className="fill-muted-foreground"
                />
                <YAxis
                  tick={{ fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                  width={30}
                  className="fill-muted-foreground"
                />
                <Bar dataKey="minutes" radius={[4, 4, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell
                      key={index}
                      className={entry.minutes > 0 ? "fill-primary" : "fill-border"}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Piece Stats */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-3 font-semibold text-foreground">곡별 연습 비율</h2>
          <div className="space-y-3">
            {stats.pieceStats.map((piece) => (
              <div key={piece.pieceId} className="space-y-1">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-foreground">{piece.title}</span>
                  <span className="text-muted-foreground">{piece.percent}%</span>
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
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <div className="mb-3 flex items-center gap-2">
            <Target className="h-5 w-5 text-primary" />
            <h2 className="font-semibold text-foreground">일일 목표</h2>
          </div>
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-foreground">하루 목표</span>
              <span className="font-medium text-foreground">
                {goals.daily.targetMinutes}분
              </span>
            </div>
            <Progress value={goals.daily.percent} className="h-3" />
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                {goals.daily.achievedMinutes}/{goals.daily.targetMinutes}분
              </p>
              <span className={`text-lg font-bold ${goals.daily.percent >= 100 ? "text-success" : "text-primary"}`}>
                {goals.daily.percent}%
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Weekly Goal */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-3 font-semibold text-foreground">주간 목표</h2>
          <div className="space-y-3">
            <div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-foreground">연습 일수</span>
                <span className="text-foreground">
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
                <span className="text-foreground">연습 시간</span>
                <span className="text-foreground">
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
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5 text-center">
          <div className="mx-auto mb-1 flex h-12 w-12 items-center justify-center rounded-full bg-orange-100 dark:bg-orange-950">
            <Flame className="h-6 w-6 text-orange-500" />
          </div>
          <p className="text-2xl font-bold text-[--streak]">
            {goals.streak.currentDays}일 연속 달성!
          </p>
          <p className="mt-1 text-sm text-muted-foreground">
            최장 스트릭: {goals.streak.longestDays}일
          </p>
        </CardContent>
      </Card>

      {/* Piece Goals */}
      {goals.pieceGoals.length > 0 && (
        <Card className="card-elevated">
          <CardContent className="px-5 pb-5 pt-5">
            <h2 className="mb-3 font-semibold text-foreground">곡 완성 목표</h2>
            <div className="space-y-4">
              {goals.pieceGoals.map((g) => (
                <div key={g.id} className="space-y-1.5">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium text-foreground">{g.piece.title}</span>
                    <Badge variant="outline" className="text-xs font-medium">
                      D-{g.daysRemaining}
                    </Badge>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    목표일: {g.targetDate}
                  </p>
                  <div className="flex items-center gap-2">
                    <Progress
                      value={g.currentProgressPercent}
                      className="h-2 flex-1"
                    />
                    <span className={`text-sm font-semibold ${g.currentProgressPercent >= 80 ? "text-success" : "text-foreground"}`}>
                      {g.currentProgressPercent}%
                    </span>
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
