"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Play } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
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
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#3F51B5] border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-4 px-5 py-6">
      <h1 className="text-2xl font-bold">연습 기록</h1>

      {/* View Toggle */}
      <div className="flex gap-2">
        <Button
          variant={viewMode === "daily" ? "default" : "outline"}
          size="sm"
          onClick={() => setViewMode("daily")}
          className={viewMode === "daily" ? "bg-[#3F51B5]" : ""}
        >
          일별
        </Button>
        <Button
          variant={viewMode === "weekly" ? "default" : "outline"}
          size="sm"
          onClick={() => setViewMode("weekly")}
          className={viewMode === "weekly" ? "bg-[#3F51B5]" : ""}
        >
          주별
        </Button>
      </div>

      {/* Sessions List */}
      <div className="space-y-3">
        {sessions.map((session) => (
          <div key={session.id}>
            <div className="mb-1 flex items-center justify-between text-sm text-[#616161]">
              <span>{formatDate(session.startedAt)}</span>
              <span>총 {formatDuration(session.totalDurationSeconds)}</span>
            </div>
            <Card className="transition-shadow hover:shadow-md">
              <CardContent className="space-y-2 pt-4">
                {session.pieces.map((piece) => (
                  <div
                    key={piece.pieceId}
                    className="flex items-center justify-between"
                  >
                    <span className="text-sm">{piece.title}</span>
                    <span className="text-sm text-[#616161]">
                      {formatDuration(piece.durationSeconds)}
                    </span>
                  </div>
                ))}
                {session.mood && (
                  <div className="flex items-center gap-1 pt-1 text-sm text-[#616161]">
                    <span>컨디션:</span>
                    <span>{MOOD_EMOJI[session.mood]}</span>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        ))}
      </div>

      {/* Start Practice Button */}
      <div className="fixed bottom-20 left-1/2 -translate-x-1/2">
        <Button
          onClick={() => router.push("/practice/timer")}
          className="flex h-12 items-center gap-2 rounded-full bg-[#3F51B5] px-6 shadow-lg hover:bg-[#283593]"
        >
          <Play className="h-5 w-5 fill-current" />
          연습 시작
        </Button>
      </div>
    </div>
  );
}
