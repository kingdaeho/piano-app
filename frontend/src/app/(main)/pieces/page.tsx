"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, Search } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { usePieces } from "@/api/hooks";
import { StarRating } from "@/components/common/star-rating";
import { PIECE_STATUS_LABEL } from "@/lib/format";
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
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#3F51B5] border-t-transparent" />
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
    <div className="space-y-4 px-5 py-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">곡 관리</h1>
        <Button
          onClick={() => router.push("/pieces/new")}
          size="sm"
          className="bg-[#3F51B5] hover:bg-[#283593]"
        >
          <Plus className="mr-1 h-4 w-4" />곡 추가
        </Button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#9E9E9E]" />
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
              className={statusFilter === f.key ? "bg-[#3F51B5]" : ""}
            >
              {f.label}({count})
            </Button>
          );
        })}
      </div>

      {/* Pieces List */}
      {statusOrder.map((status) => {
        const items = grouped[status];
        if (!items || items.length === 0) return null;
        return (
          <div key={status}>
            <p className="mb-2 text-sm font-medium text-[#616161]">
              {PIECE_STATUS_LABEL[status]} ({items.length})
            </p>
            <div className="space-y-3">
              {items.map((piece) => (
                <Card
                  key={piece.id}
                  className="cursor-pointer transition-shadow hover:shadow-md"
                  onClick={() => router.push(`/pieces/${piece.id}`)}
                >
                  <CardContent className="space-y-2 pt-4">
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="font-medium">{piece.title}</p>
                        {piece.composer && (
                          <p className="text-sm text-[#616161]">
                            {piece.composer}
                          </p>
                        )}
                      </div>
                      {piece.status === "COMPLETED" && (
                        <span className="text-lg text-[#4CAF50]">✓</span>
                      )}
                    </div>
                    {piece.difficulty && (
                      <StarRating
                        value={piece.difficulty}
                        readonly
                        size={16}
                      />
                    )}
                    <div className="flex items-center gap-2">
                      <Progress
                        value={piece.progressPercent}
                        className="h-2 flex-1"
                      />
                      <span className="text-sm text-[#616161]">
                        {piece.progressPercent}%
                      </span>
                      <Badge
                        variant="outline"
                        className="text-xs"
                      >
                        {PIECE_STATUS_LABEL[piece.status]}
                      </Badge>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
