"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Pause, Play, Square, ChevronRight, Music } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useTimerStore } from "@/stores/timer-store";
import { useTimerWorker } from "@/hooks/use-timer-worker";
import { PieceSelector } from "@/components/practice/piece-selector";
import { useStartSession, useSwitchPiece } from "@/api/hooks";
import { formatDurationTimer, formatDuration } from "@/lib/format";
import { ProgressRing } from "@/components/common/progress-ring";
import { toast } from "sonner";

export default function TimerPage() {
  const router = useRouter();
  const [showPieceSelector, setShowPieceSelector] = useState(false);
  const [showStartSelector, setShowStartSelector] = useState(true);

  const {
    isRunning,
    isPaused,
    elapsedSeconds,
    currentPieceId,
    currentPieceTitle,
    currentPieceStartTime,
    practicedPieces,
    memo,
    sessionId,
    start,
    pause,
    resume,
    switchPiece,
    setMemo,
    setSessionId,
    stop,
  } = useTimerStore();

  const startSession = useStartSession();
  const switchPieceApi = useSwitchPiece();

  useTimerWorker();

  // Target: 60 minutes
  const targetSeconds = 60 * 60;
  const percent = Math.min(
    100,
    Math.round((elapsedSeconds / targetSeconds) * 100)
  );

  useEffect(() => {
    if (isRunning) {
      setShowStartSelector(false);
    }
  }, [isRunning]);

  const handleStart = async (pieceId: number, pieceTitle: string) => {
    try {
      const session = await startSession.mutateAsync(pieceId);
      setSessionId(session.id);
      start(pieceId, pieceTitle);
      setShowStartSelector(false);
    } catch {
      toast.error("연습 세션 시작에 실패했습니다");
    }
  };

  const handleStop = () => {
    stop();
    router.push("/practice/summary");
  };

  if (showStartSelector && !isRunning) {
    return (
      <div className="space-y-6 px-5 py-6">
        <h1 className="text-2xl font-bold">연습 시작</h1>
        <p className="text-[#616161]">연습할 곡을 선택하세요</p>
        <PieceSelector
          open={true}
          onOpenChange={(open) => {
            if (!open) router.back();
          }}
          selectedPieceId={null}
          onSelect={handleStart}
        />
      </div>
    );
  }

  return (
    <div className="flex min-h-screen flex-col items-center px-5 py-6">
      <div className="mb-2 flex w-full items-center justify-between">
        <h1 className="text-lg font-semibold text-[#3F51B5]">연습 중</h1>
      </div>

      {/* Timer Ring */}
      <div className="my-8">
        <ProgressRing percent={percent} size={220} strokeWidth={14}>
          <span className="font-mono text-4xl font-bold text-[#212121]">
            {formatDurationTimer(elapsedSeconds)}
          </span>
        </ProgressRing>
      </div>

      {/* Current Piece */}
      <Card className="mb-4 w-full">
        <CardContent className="pt-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Music className="h-5 w-5 text-[#3F51B5]" />
              <div>
                <p className="font-medium">{currentPieceTitle}</p>
                <p className="text-sm text-[#616161]">
                  이 곡 연습 시간: {formatDuration(currentPieceStartTime)}
                </p>
              </div>
            </div>
            <button
              onClick={() => setShowPieceSelector(true)}
              className="flex items-center text-sm text-[#3F51B5]"
            >
              곡 변경 <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </CardContent>
      </Card>

      {/* Practiced Pieces */}
      {practicedPieces.length > 0 && (
        <Card className="mb-4 w-full">
          <CardContent className="pt-4">
            <p className="mb-2 text-sm font-medium text-[#616161]">
              오늘 연습한 곡
            </p>
            {practicedPieces.map((piece) => (
              <div
                key={piece.pieceId}
                className="flex items-center justify-between py-1"
              >
                <span className="text-sm">{piece.title}</span>
                <span className="text-sm text-[#616161]">
                  {formatDuration(piece.durationSeconds)}
                </span>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* Memo */}
      <Card className="mb-6 w-full">
        <CardContent className="pt-4">
          <p className="mb-2 text-sm font-medium text-[#616161]">연습 메모</p>
          <Textarea
            placeholder="오늘 연습에 대한 메모를 남겨보세요..."
            value={memo}
            onChange={(e) => setMemo(e.target.value)}
            className="min-h-[80px] resize-none"
          />
        </CardContent>
      </Card>

      {/* Controls */}
      <div className="flex items-center gap-6">
        {isPaused ? (
          <Button
            onClick={resume}
            size="lg"
            className="h-16 w-16 rounded-full bg-[#3F51B5] hover:bg-[#283593]"
          >
            <Play className="h-8 w-8 fill-current" />
          </Button>
        ) : (
          <Button
            onClick={pause}
            size="lg"
            className="h-16 w-16 rounded-full bg-[#3F51B5] hover:bg-[#283593]"
          >
            <Pause className="h-8 w-8" />
          </Button>
        )}
        <Button
          onClick={handleStop}
          size="lg"
          variant="destructive"
          className="flex h-16 items-center gap-2 rounded-full px-8"
        >
          <Square className="h-6 w-6 fill-current" />
          종료
        </Button>
      </div>

      <PieceSelector
        open={showPieceSelector}
        onOpenChange={setShowPieceSelector}
        selectedPieceId={currentPieceId}
        onSelect={async (pieceId, pieceTitle) => {
          if (sessionId) {
            try {
              await switchPieceApi.mutateAsync({ sessionId, pieceId });
            } catch {
              toast.error("곡 변경에 실패했습니다");
              return;
            }
          }
          switchPiece(pieceId, pieceTitle);
        }}
      />
    </div>
  );
}
