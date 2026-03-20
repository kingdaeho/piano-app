"use client";

import { useRouter } from "next/navigation";
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
    <div className="space-y-6 px-5 py-6">
      <div className="text-center">
        <h1 className="text-2xl font-bold">연습 완료!</h1>
        <p className="mt-2 text-sm text-[#616161]">오늘 총 연습 시간</p>
        <p className="mt-1 text-4xl font-bold text-[#3F51B5]">
          {formatDuration(elapsedSeconds || totalSeconds)}
        </p>
      </div>

      {/* Per-piece breakdown */}
      <Card>
        <CardContent className="space-y-3 pt-6">
          <h2 className="font-medium">곡별 연습 시간</h2>
          {practicedPieces.map((piece) => {
            const percent = totalSeconds > 0
              ? Math.round((piece.durationSeconds / totalSeconds) * 100)
              : 0;
            return (
              <div key={piece.pieceId} className="space-y-1">
                <div className="flex items-center justify-between text-sm">
                  <span>{piece.title}</span>
                  <span className="text-[#616161]">
                    {formatDuration(piece.durationSeconds)}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <Progress value={percent} className="h-2 flex-1" />
                  <span className="text-xs text-[#616161]">{percent}%</span>
                </div>
              </div>
            );
          })}
        </CardContent>
      </Card>

      {/* Mood */}
      <Card>
        <CardContent className="pt-6">
          <h2 className="mb-3 font-medium">오늘 연습 어땠나요?</h2>
          <div className="flex justify-around">
            {moods.map((m) => (
              <button
                key={m}
                onClick={() => setMood(m)}
                className={cn(
                  "flex flex-col items-center gap-1 rounded-lg px-3 py-2 transition-colors",
                  mood === m ? "bg-[#E8EAF6]" : "hover:bg-gray-50"
                )}
              >
                <span className="text-2xl">{MOOD_EMOJI[m]}</span>
                <span className="text-xs text-[#616161]">{MOOD_LABEL[m]}</span>
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Goal Progress */}
      <Card>
        <CardContent className="pt-6">
          <h2 className="mb-2 font-medium">목표 달성률</h2>
          <div className="flex items-center gap-3">
            <Progress value={goalPercent} className="h-3 flex-1" />
            <span className="text-sm font-medium text-[#3F51B5]">
              {goalPercent}%
            </span>
          </div>
          <p className="mt-1 text-sm text-[#616161]">
            {achievedMinutes}/{goalMinutes}분
          </p>
        </CardContent>
      </Card>

      <Button
        onClick={handleSave}
        className="w-full bg-[#3F51B5] hover:bg-[#283593]"
        size="lg"
      >
        저장하기
      </Button>
    </div>
  );
}
