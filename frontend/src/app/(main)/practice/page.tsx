"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Play, Clock } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/common/empty-state";
import { usePracticeSessions } from "@/api/hooks";
import {
  formatDate,
  formatDuration,
  MOOD_EMOJI,
} from "@/lib/format";

export default function PracticeHistoryPage() {
  const { data: sessions, isLoading } = usePracticeSessions();
  const router = useRouter();
  const [viewMode, setViewMode] = useState<"daily" | "weekly">("daily");

  if (isLoading || !sessions) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-5 px-5 pb-8 pt-7">
      <h1 className="text-2xl font-extrabold tracking-tight text-foreground">연습 기록</h1>

      {/* View Toggle */}
      <div className="flex gap-2">
        <Button
          variant={viewMode === "daily" ? "default" : "outline"}
          size="sm"
          onClick={() => setViewMode("daily")}
        >
          일별
        </Button>
        <Button
          variant={viewMode === "weekly" ? "default" : "outline"}
          size="sm"
          onClick={() => setViewMode("weekly")}
        >
          주별
        </Button>
      </div>

      {/* Sessions List */}
      {sessions.length === 0 ? (
        <EmptyState
          icon={<Clock className="h-8 w-8" />}
          title="연습 기록이 없습니다"
          description="첫 연습을 시작해보세요!"
          actionLabel="연습 시작"
          onAction={() => router.push("/practice/timer")}
        />
      ) : (
        <div className="space-y-4">
          {sessions.map((session) => (
            <div key={session.id}>
              <div className="mb-1.5 flex items-center justify-between text-sm">
                <span className="font-medium text-foreground">{formatDate(session.startedAt)}</span>
                <span className="text-muted-foreground">총 {formatDuration(session.totalDurationSeconds)}</span>
              </div>
              <Card className="card-elevated">
                <CardContent className="space-y-2.5 px-5 pb-4 pt-4">
                  {session.pieces.map((piece) => (
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
                  {session.mood && (
                    <div className="flex items-center gap-1.5 border-t border-border pt-2.5 text-sm text-muted-foreground">
                      <span>컨디션:</span>
                      <span>{MOOD_EMOJI[session.mood]}</span>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          ))}
        </div>
      )}

      {/* Start Practice Button - H3: CTA prominence */}
      <div className="fixed bottom-20 left-1/2 -translate-x-1/2">
        <Button
          onClick={() => router.push("/practice/timer")}
          className="btn-cta flex h-12 items-center gap-2 rounded-full px-7 text-primary-foreground"
          size="lg"
        >
          <Play className="h-5 w-5 fill-current" />
          연습 시작
        </Button>
      </div>
    </div>
  );
}
