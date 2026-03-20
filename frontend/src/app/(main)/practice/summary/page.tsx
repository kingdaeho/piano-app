"use client";

import { useRouter } from "next/navigation";
import { CheckCircle2, Clock, Music, Target } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { useTimerStore } from "@/stores/timer-store";
import { useEndSession } from "@/api/hooks";
import { formatDuration } from "@/lib/format";
import { MOOD_EMOJI, MOOD_LABEL } from "@/lib/format";
import { toast } from "sonner";
import type { Mood } from "@/types";
import { cn } from "@/lib/utils";

const moods: Mood[] = ["TERRIBLE", "BAD", "OK", "GOOD", "GREAT"];

export default function PracticeSummaryPage() {
  const router = useRouter();
  const { elapsedSeconds, practicedPieces, mood, memo, sessionId, setMood, reset } =
    useTimerStore();
  const endSession = useEndSession();

  const totalSeconds = practicedPieces.reduce(
    (sum, p) => sum + p.durationSeconds,
    0
  );

  // Goal: 60 minutes
  const goalMinutes = 60;
  const achievedMinutes = Math.round(totalSeconds / 60);
  const goalPercent = Math.min(
    100,
    Math.round((achievedMinutes / goalMinutes) * 100)
  );

  const handleSave = async () => {
    if (sessionId) {
      try {
        await endSession.mutateAsync({
          sessionId,
          data: {
            memo: memo || undefined,
            mood: mood || undefined,
          },
        });
      } catch {
        toast.error("연습 기록 저장에 실패했습니다");
        return;
      }
    }
    reset();
    router.push("/home");
  };

  return (
    <div className="space-y-6 px-5 pb-8 pt-8">
      <div className="flex flex-col items-center space-y-3 text-center">
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-success/15">
          <CheckCircle2 className="h-9 w-9 text-success" />
        </div>
        <h1 className="text-[1.75rem] font-extrabold tracking-tight text-foreground">연습 완료!</h1>
        <p className="text-sm text-muted-foreground">수고하셨습니다!</p>
        <p className="text-5xl font-bold tracking-tight text-primary">
          {formatDuration(elapsedSeconds || totalSeconds)}
        </p>
      </div>

      {/* Per-piece breakdown */}
      <Card className="card-elevated">
        <CardContent className="space-y-3 px-5 pb-5 pt-5">
          <div className="flex items-center gap-2">
            <Music className="h-4 w-4 text-primary" />
            <h2 className="font-semibold text-foreground">곡별 연습 시간</h2>
          </div>
          {practicedPieces.map((piece) => {
            const percent = totalSeconds > 0
              ? Math.round((piece.durationSeconds / totalSeconds) * 100)
              : 0;
            return (
              <div key={piece.pieceId} className="space-y-1.5">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-foreground">{piece.title}</span>
                  <span className="font-medium text-muted-foreground">
                    {formatDuration(piece.durationSeconds)}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <Progress value={percent} className="h-2 flex-1" />
                  <span className="text-xs font-medium text-muted-foreground">{percent}%</span>
                </div>
              </div>
            );
          })}
        </CardContent>
      </Card>

      {/* Mood */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-4 font-semibold text-foreground">오늘 연습 어땠나요?</h2>
          <div className="flex justify-around">
            {moods.map((m) => (
              <button
                key={m}
                onClick={() => setMood(m)}
                className={cn(
                  "flex flex-col items-center gap-1.5 rounded-xl px-3 py-2.5 transition-all",
                  mood === m
                    ? "bg-accent scale-105 shadow-sm"
                    : "hover:bg-muted"
                )}
              >
                <span className="text-2xl">{MOOD_EMOJI[m]}</span>
                <span className="text-[11px] font-medium text-muted-foreground">{MOOD_LABEL[m]}</span>
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Goal Progress */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <div className="mb-3 flex items-center gap-2">
            <Target className="h-4 w-4 text-primary" />
            <h2 className="font-semibold text-foreground">목표 달성률</h2>
          </div>
          <div className="flex items-center gap-3">
            <Progress value={goalPercent} className="h-3 flex-1" />
            <span className="text-sm font-bold text-primary">
              {goalPercent}%
            </span>
          </div>
          <p className="mt-1.5 text-sm text-muted-foreground">
            {achievedMinutes}/{goalMinutes}분
          </p>
        </CardContent>
      </Card>

      <Button
        onClick={handleSave}
        className="btn-cta w-full rounded-xl text-primary-foreground"
        size="lg"
      >
        저장하기
      </Button>
    </div>
  );
}
