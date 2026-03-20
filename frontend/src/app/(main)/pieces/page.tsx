"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, Search, Music, Music2 } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { EmptyState } from "@/components/common/empty-state";
import { usePieces } from "@/api/hooks";
import { StarRating } from "@/components/common/star-rating";
import { PIECE_STATUS_LABEL } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { PieceStatus } from "@/types";

const statusFilters: { key: PieceStatus | "ALL"; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "PRACTICING", label: "연습 중" },
  { key: "COMPLETED", label: "완성" },
  { key: "FINISHING", label: "마무리" },
  { key: "ON_HOLD", label: "보류" },
  { key: "NOT_STARTED", label: "시작 전" },
];

export default function PiecesPage() {
  const { data: pieces, isLoading } = usePieces();
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<PieceStatus | "ALL">("ALL");

  if (isLoading || !pieces) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }

  const filtered = pieces.filter((p) => {
    const matchesSearch =
      search === "" ||
      p.title.toLowerCase().includes(search.toLowerCase()) ||
      p.composer?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || p.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  // Group by status
  const grouped = filtered.reduce(
    (acc, piece) => {
      if (!acc[piece.status]) {
        acc[piece.status] = [];
      }
      acc[piece.status].push(piece);
      return acc;
    },
    {} as Record<string, typeof filtered>
  );

  const statusOrder: PieceStatus[] = [
    "PRACTICING",
    "FINISHING",
    "COMPLETED",
    "ON_HOLD",
    "NOT_STARTED",
  ];

  return (
    <div className="space-y-5 px-5 pb-8 pt-7">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold tracking-tight text-foreground">곡 관리</h1>
        <Button
          onClick={() => router.push("/pieces/new")}
          size="sm"
        >
          <Plus className="mr-1 h-4 w-4" />곡 추가
        </Button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="곡명, 작곡가로 검색..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10"
        />
      </div>

      {/* Filter Chips */}
      <div className="flex flex-wrap gap-2">
        {statusFilters.map((f) => {
          const count =
            f.key === "ALL"
              ? pieces.length
              : pieces.filter((p) => p.status === f.key).length;
          return (
            <Button
              key={f.key}
              variant={statusFilter === f.key ? "default" : "outline"}
              size="sm"
              onClick={() => setStatusFilter(f.key)}
            >
              {f.label}({count})
            </Button>
          );
        })}
      </div>

      {/* Pieces List */}
      {filtered.length === 0 ? (
        <EmptyState
          icon={<Music className="h-8 w-8" />}
          title="등록된 곡이 없습니다"
          description="연습할 곡을 추가해보세요"
          actionLabel="곡 추가"
          onAction={() => router.push("/pieces/new")}
        />
      ) : (
        statusOrder.map((status) => {
          const items = grouped[status];
          if (!items || items.length === 0) return null;
          return (
            <div key={status}>
              <p className="mb-2 text-sm font-medium text-muted-foreground">
                {PIECE_STATUS_LABEL[status]} ({items.length})
              </p>
              <div className="space-y-3">
                {items.map((piece) => (
                  <Card
                    key={piece.id}
                    className="card-elevated cursor-pointer shadow-sm transition-shadow hover:shadow-md"
                    onClick={() => router.push(`/pieces/${piece.id}`)}
                  >
                    <CardContent className="space-y-2.5 px-5 pb-4 pt-4">
                      <div className="flex items-start justify-between">
                        <div className="flex items-start gap-3">
                          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-accent">
                            <Music2 className="h-4 w-4 text-accent-foreground" />
                          </div>
                          <div>
                            <p className="font-medium text-foreground">{piece.title}</p>
                            {piece.composer && (
                              <p className="text-sm text-muted-foreground">
                                {piece.composer}
                              </p>
                            )}
                          </div>
                        </div>
                        {piece.status === "COMPLETED" && (
                          <span className="text-lg text-success">✓</span>
                        )}
                      </div>
                      <div className="flex items-center gap-2">
                        {piece.difficulty && (
                          <Badge
                            variant="outline"
                            className={cn(
                              "text-xs",
                              piece.difficulty <= 2
                                ? "border-green-300 bg-green-50 text-green-700 dark:border-green-700 dark:bg-green-950 dark:text-green-400"
                                : piece.difficulty <= 3
                                  ? "border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-400"
                                  : "border-red-300 bg-red-50 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-400"
                            )}
                          >
                            난이도 {"★".repeat(piece.difficulty)}
                          </Badge>
                        )}
                        <Badge
                          variant="outline"
                          className={cn(
                            "text-xs",
                            piece.status === "PRACTICING" || piece.status === "FINISHING"
                              ? "border-primary/30 bg-primary/10 text-primary"
                              : piece.status === "COMPLETED"
                                ? "border-green-300 bg-green-50 text-green-700 dark:border-green-700 dark:bg-green-950 dark:text-green-400"
                                : ""
                          )}
                        >
                          {PIECE_STATUS_LABEL[piece.status]}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-2">
                        <Progress
                          value={piece.progressPercent}
                          className="h-2 flex-1"
                        />
                        <span className="text-sm text-muted-foreground">
                          {piece.progressPercent}%
                        </span>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          );
        })
      )}
    </div>
  );
}
