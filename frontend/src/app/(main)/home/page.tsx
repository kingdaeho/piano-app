"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Play, ChevronRight, Flame } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { ProgressRing } from "@/components/common/progress-ring";
import { useDashboard } from "@/api/hooks";
import { useAuthStore } from "@/stores/auth-store";
import {
  formatDurationHM,
  formatDate,
  PIECE_STATUS_LABEL,
} from "@/lib/format";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { getDayOfWeek } from "@/lib/format";

export default function HomePage() {
  const { data: dashboard, isLoading } = useDashboard();
  const user = useAuthStore((s) => s.user);
  const router = useRouter();

  if (isLoading || !dashboard) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#3F51B5] border-t-transparent" />
      </div>
    );
  }

  const chartData = dashboard.weeklyChart.map((d) => ({
    day: getDayOfWeek(d.date),
    minutes: Math.round(d.durationSeconds / 60),
  }));

  const totalWeekMinutes = dashboard.weeklyChart.reduce(
    (sum, d) => sum + d.durationSeconds,
    0
  );

  return (
    <div className="space-y-5 px-5 py-6">
      {/* Greeting */}
      <div>
        <h1 className="text-2xl font-bold text-[#212121]">
          안녕하세요, {user?.name ?? ""}님
        </h1>
        <p className="text-sm text-[#616161]">
          오늘도 피아노 앞에 앉아볼까요?
        </p>
      </div>

      {/* Goal Progress Card */}
      <Card>
        <CardContent className="flex flex-col items-center gap-4 pt-6">
          <p className="text-sm font-medium text-[#616161]">
            오늘의 연습 목표
          </p>
          <ProgressRing percent={dashboard.today.percent} size={160}>
            <span className="text-2xl font-bold text-[#212121]">
              {dashboard.today.achievedMinutes} / {dashboard.today.goalMinutes}분
            </span>
            <span className="text-lg font-semibold text-[#3F51B5]">
              {dashboard.today.percent}%
            </span>
          </ProgressRing>
          <div className="flex items-center gap-4 text-sm text-[#616161]">
            <span className="flex items-center gap-1">
              <Flame className="h-4 w-4 text-[#FF6D00]" />
              {dashboard.streak.currentDays}일 연속 달성
            </span>
            <span>
              이번 주 {dashboard.streak.weeklyAchievedDays}/
              {dashboard.streak.weeklyTargetDays}일
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Quick Start */}
      <Button
        onClick={() => router.push("/practice/timer")}
        className="flex h-14 w-full items-center gap-3 bg-[#3F51B5] text-lg hover:bg-[#283593]"
      >
        <Play className="h-6 w-6 fill-current" />
        연습 시작
      </Button>

      {/* Latest Lesson Note */}
      {dashboard.latestLessonNote && (
        <div>
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-lg font-semibold">최근 레슨 노트</h2>
            <Link
              href="/lessons"
              className="flex items-center text-sm text-[#3F51B5]"
            >
              더보기 <ChevronRight className="h-4 w-4" />
            </Link>
          </div>
          <Card
            className="cursor-pointer transition-shadow hover:shadow-md"
            onClick={() =>
              router.push(`/lessons/${dashboard.latestLessonNote!.id}`)
            }
          >
            <CardContent className="space-y-2 pt-4">
              <div className="flex items-center gap-2 text-sm text-[#616161]">
                <span>
                  {dashboard.latestLessonNote.lessonNumber}회차
                </span>
                <span>&middot;</span>
                <span>
                  {formatDate(dashboard.latestLessonNote.lessonDate)}
                </span>
              </div>
              <div className="flex flex-wrap gap-1">
                {dashboard.latestLessonNote.pieces.map((p) => (
                  <Badge
                    key={p.id}
                    variant="secondary"
                    className="bg-[#E8EAF6] text-[#1A237E]"
                  >
                    {p.title}
                  </Badge>
                ))}
              </div>
              {dashboard.latestLessonNote.content && (
                <p className="line-clamp-2 text-sm text-[#616161]">
                  {dashboard.latestLessonNote.content.split("\n")[0]}
                </p>
              )}
              <div className="text-xs text-[#9E9E9E]">
                과제{" "}
                {
                  dashboard.latestLessonNote.assignments.filter(
                    (a) => a.isCompleted
                  ).length
                }
                /{dashboard.latestLessonNote.assignments.length} 완료
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Active Pieces */}
      <div>
        <div className="mb-2 flex items-center justify-between">
          <h2 className="text-lg font-semibold">연습 중인 곡</h2>
          <Link
            href="/pieces"
            className="flex items-center text-sm text-[#3F51B5]"
          >
            더보기 <ChevronRight className="h-4 w-4" />
          </Link>
        </div>
        <div className="flex gap-3 overflow-x-auto pb-2">
          {dashboard.activePieces.map((piece) => (
            <Card
              key={piece.id}
              className="min-w-[160px] cursor-pointer transition-shadow hover:shadow-md"
              onClick={() => router.push(`/pieces/${piece.id}`)}
            >
              <CardContent className="space-y-2 pt-4">
                <p className="text-sm font-medium">{piece.title}</p>
                <div className="flex items-center gap-2">
                  <Progress
                    value={piece.progressPercent}
                    className="h-2 flex-1"
                  />
                  <span className="text-xs text-[#616161]">
                    {piece.progressPercent}%
                  </span>
                </div>
                <Badge
                  variant="outline"
                  className="text-xs"
                >
                  {PIECE_STATUS_LABEL[piece.status]}
                </Badge>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Weekly Chart */}
      <div>
        <h2 className="mb-2 text-lg font-semibold">이번 주 연습</h2>
        <Card>
          <CardContent className="pt-4">
            <div className="h-[150px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <XAxis
                    dataKey="day"
                    tick={{ fontSize: 12, fill: "#616161" }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <YAxis hide />
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
            <p className="mt-2 text-center text-sm text-[#616161]">
              총 {formatDurationHM(totalWeekMinutes)}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
