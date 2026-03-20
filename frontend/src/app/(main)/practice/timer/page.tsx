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
      <div className="space-y-6 px-5 py-8">
        <div className="space-y-1">
          <h1 className="text-2xl font-extrabold tracking-tight text-foreground">연습 시작</h1>
          <p className="text-muted-foreground">연습할 곡을 선택하세요</p>
        </div>
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
    <div className="flex min-h-screen flex-col items-center px-5 pb-8 pt-6">
      <div className="mb-2 flex w-full items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75" />
            <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-primary" />
          </span>
          <h1 className="text-lg font-bold text-primary">연습 중</h1>
        </div>
      </div>

      {/* Timer Ring */}
      <div className="my-10">
        <ProgressRing percent={percent} size={240} strokeWidth={16}>
          <span className="font-mono text-6xl font-bold tracking-tight text-foreground">
            {formatDurationTimer(elapsedSeconds)}
          </span>
        </ProgressRing>
      </div>

      {/* Current Piece */}
      <Card className="card-elevated mb-4 w-full">
        <CardContent className="px-5 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent">
                <Music className="h-5 w-5 text-primary" />
              </div>
              <div>
                <p className="font-semibold text-foreground">{currentPieceTitle}</p>
                <p className="text-sm text-muted-foreground">
                  {formatDuration(currentPieceStartTime)}
                </p>
              </div>
            </div>
            <button
              onClick={() => setShowPieceSelector(true)}
              className="flex items-center rounded-lg px-2 py-1 text-sm font-medium text-primary hover:bg-accent"
            >
              곡 변경 <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </CardContent>
      </Card>

      {/* Practiced Pieces */}
      {practicedPieces.length > 0 && (
        <Card className="card-elevated mb-4 w-full">
          <CardContent className="px-5 py-4">
            <p className="section-heading mb-3">
              오늘 연습한 곡
            </p>
            <div className="space-y-2">
              {practicedPieces.map((piece) => (
                <div
                  key={piece.pieceId}
                  className="flex items-center justify-between"
                >
                  <span className="text-sm text-foreground">{piece.title}</span>
                  <span className="text-sm font-medium text-muted-foreground">
                    {formatDuration(piece.durationSeconds)}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Memo */}
      <Card className="card-elevated mb-8 w-full">
        <CardContent className="px-5 py-4">
          <p className="section-heading mb-3">연습 메모</p>
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
            className="btn-cta h-18 w-18 rounded-full text-primary-foreground"
          >
            <Play className="h-9 w-9 fill-current" />
          </Button>
        ) : (
          <Button
            onClick={pause}
            size="lg"
            className="btn-cta h-18 w-18 rounded-full text-primary-foreground"
          >
            <Pause className="h-9 w-9" />
          </Button>
        )}
        <Button
          onClick={handleStop}
          size="lg"
          variant="destructive"
          className="flex h-14 items-center gap-2 rounded-full px-8 shadow-lg"
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
