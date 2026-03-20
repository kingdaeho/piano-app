"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft, Pencil } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { usePiece } from "@/api/hooks";
import { StarRating } from "@/components/common/star-rating";
import { PIECE_STATUS_LABEL, formatDateFull } from "@/lib/format";

export default function PieceDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const pieceId = parseInt(id, 10);
  const { data: piece, isLoading } = usePiece(pieceId);
  const router = useRouter();

  if (isLoading || !piece) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-5 px-5 pb-8 pt-7">
      {/* Header */}
      <div className="flex items-center justify-between">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => router.back()}
        >
          <ChevronLeft className="h-6 w-6" />
        </Button>
        <div />
        <Button variant="ghost" size="icon" className="text-primary">
          <Pencil className="h-5 w-5" />
        </Button>
      </div>

      {/* Title */}
      <div>
        <h1 className="text-xl font-bold text-foreground">{piece.title}</h1>
        {piece.composer && (
          <p className="text-muted-foreground">{piece.composer}</p>
        )}
      </div>

      {/* Info */}
      <Card className="card-elevated">
        <CardContent className="space-y-3 px-5 pb-5 pt-5">
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">상태</span>
            <Badge className="bg-accent text-accent-foreground">
              {PIECE_STATUS_LABEL[piece.status]}
            </Badge>
          </div>
          {piece.difficulty && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">난이도</span>
              <StarRating value={piece.difficulty} readonly size={16} />
            </div>
          )}
          {piece.genre && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">장르/교재</span>
              <span className="text-sm text-foreground">{piece.genre}</span>
            </div>
          )}
          {piece.startedAt && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">등록일</span>
              <span className="text-sm text-foreground">
                {formatDateFull(piece.startedAt)}
              </span>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Progress */}
      <Card className="card-elevated">
        <CardContent className="px-5 pb-5 pt-5">
          <h2 className="mb-3 font-semibold text-foreground">진행도</h2>
          <div className="flex items-center gap-3">
            <Progress value={piece.progressPercent} className="h-3 flex-1" />
            <span className="text-lg font-bold text-primary">
              {piece.progressPercent}%
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Memo */}
      {piece.memo && (
        <Card className="card-elevated">
          <CardContent className="px-5 pb-5 pt-5">
            <h2 className="mb-2 font-semibold text-foreground">메모</h2>
            <p className="text-sm text-muted-foreground">{piece.memo}</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
