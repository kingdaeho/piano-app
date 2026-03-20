"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Play, ChevronRight, Flame, Music } from "lucide-react";
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
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
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
    <div className="space-y-6 px-5 pb-8 pt-7">
      {/* Greeting - H1: stronger typography hierarchy */}
      <div className="space-y-1">
        <p className="text-sm font-medium text-muted-foreground">
          안녕하세요,
        </p>
        <h1 className="text-[1.75rem] font-extrabold tracking-tight text-foreground">
          {user?.name ?? ""}님
        </h1>
        <p className="text-sm text-muted-foreground">
          오늘도 피아노 앞에 앉아볼까요?
        </p>
      </div>

      {/* Goal Progress Card - C1: card depth, C2: more padding */}
      <Card className="card-elevated overflow-hidden bg-gradient-to-br from-card via-card to-accent/30">
        <CardContent className="flex flex-col items-center gap-5 px-6 pb-6 pt-7">
          <p className="section-heading">
            오늘의 연습 목표
          </p>
          <ProgressRing percent={dashboard.today.percent} size={172} strokeWidth={14}>
            <span className="text-[1.625rem] font-bold text-foreground">
              {dashboard.today.achievedMinutes} / {dashboard.today.goalMinutes}분
            </span>
            <span className="text-lg font-semibold text-primary">
              {dashboard.today.percent}%
            </span>
          </ProgressRing>
          <div className="flex items-center gap-5 text-sm text-muted-foreground">
            <span className="flex items-center gap-1.5 font-medium">
              <Flame className="h-4 w-4 text-[--streak]" />
              {dashboard.streak.currentDays}일 연속
            </span>
            <span className="h-4 w-px bg-border" />
            <span>
              이번 주 {dashboard.streak.weeklyAchievedDays}/
              {dashboard.streak.weeklyTargetDays}일
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Quick Start - H3: CTA prominence */}
      <Button
        onClick={() => router.push("/practice/timer")}
        className="btn-cta flex h-14 w-full items-center gap-3 rounded-xl text-lg font-semibold text-primary-foreground"
        size="lg"
      >
        <Play className="h-6 w-6 fill-current" />
        연습 시작
      </Button>

      {/* Latest Lesson Note */}
      {dashboard.latestLessonNote && (
        <section>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-bold tracking-tight text-foreground">최근 레슨 노트</h2>
            <Link
              href="/lessons"
              className="flex items-center text-sm font-medium text-primary"
            >
              더보기 <ChevronRight className="h-4 w-4" />
            </Link>
          </div>
          <Card
            className="card-elevated cursor-pointer"
            onClick={() =>
              router.push(`/lessons/${dashboard.latestLessonNote!.id}`)
            }
          >
            <CardContent className="space-y-2.5 px-5 pb-4 pt-5">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="font-medium text-foreground">
                  {dashboard.latestLessonNote.lessonNumber}회차
                </span>
                <span>&middot;</span>
                <span>
                  {formatDate(dashboard.latestLessonNote.lessonDate)}
                </span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {dashboard.latestLessonNote.pieces.map((p) => (
                  <Badge
                    key={p.id}
                    variant="secondary"
                    className="bg-accent text-accent-foreground"
                  >
                    {p.title}
                  </Badge>
                ))}
              </div>
              {dashboard.latestLessonNote.content && (
                <p className="line-clamp-2 text-sm leading-relaxed text-muted-foreground">
                  {dashboard.latestLessonNote.content.split("\n")[0]}
                </p>
              )}
              <div className="text-xs font-medium text-muted-foreground">
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
        </section>
      )}

      {/* Active Pieces */}
      <section>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-base font-bold tracking-tight text-foreground">연습 중인 곡</h2>
          <Link
            href="/pieces"
            className="flex items-center text-sm font-medium text-primary"
          >
            더보기 <ChevronRight className="h-4 w-4" />
          </Link>
        </div>
        <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-none">
          {dashboard.activePieces.map((piece) => (
            <Card
              key={piece.id}
              className="card-elevated min-w-[168px] cursor-pointer"
              onClick={() => router.push(`/pieces/${piece.id}`)}
            >
              <CardContent className="space-y-2.5 px-4 pb-4 pt-4">
                <div className="flex items-center gap-2">
                  <Music className="h-4 w-4 shrink-0 text-primary" />
                  <p className="text-sm font-semibold text-foreground">{piece.title}</p>
                </div>
                <div className="flex items-center gap-2">
                  <Progress
                    value={piece.progressPercent}
                    className="h-2 flex-1"
                  />
                  <span className="text-xs font-medium text-muted-foreground">
                    {piece.progressPercent}%
                  </span>
                </div>
                <Badge
                  variant="outline"
                  className="text-[11px]"
                >
                  {PIECE_STATUS_LABEL[piece.status]}
                </Badge>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Weekly Chart */}
      <section>
        <h2 className="mb-3 text-base font-bold tracking-tight text-foreground">이번 주 연습</h2>
        <Card className="card-elevated">
          <CardContent className="px-5 pb-5 pt-5">
            <div className="h-[160px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} barCategoryGap="25%">
                  <XAxis
                    dataKey="day"
                    tick={{ fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    className="fill-muted-foreground"
                  />
                  <YAxis hide />
                  <Bar dataKey="minutes" radius={[6, 6, 0, 0]}>
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
            <p className="mt-3 text-center text-sm font-medium text-muted-foreground">
              총 {formatDurationHM(totalWeekMinutes)}
            </p>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
