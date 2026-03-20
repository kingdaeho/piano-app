"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft, Pencil } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
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
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#3F51B5] border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-4 px-5 py-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button onClick={() => router.back()}>
          <ChevronLeft className="h-6 w-6" />
        </button>
        <div />
        <button className="text-[#3F51B5]">
          <Pencil className="h-5 w-5" />
        </button>
      </div>

      {/* Title */}
      <div>
        <h1 className="text-xl font-bold">{piece.title}</h1>
        {piece.composer && (
          <p className="text-[#616161]">{piece.composer}</p>
        )}
      </div>

      {/* Info */}
      <Card>
        <CardContent className="space-y-3 pt-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-[#616161]">상태</span>
            <Badge className="bg-[#E8EAF6] text-[#1A237E]">
              {PIECE_STATUS_LABEL[piece.status]}
            </Badge>
          </div>
          {piece.difficulty && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-[#616161]">난이도</span>
              <StarRating value={piece.difficulty} readonly size={16} />
            </div>
          )}
          {piece.genre && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-[#616161]">장르/교재</span>
              <span className="text-sm">{piece.genre}</span>
            </div>
          )}
          {piece.startedAt && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-[#616161]">등록일</span>
              <span className="text-sm">
                {formatDateFull(piece.startedAt)}
              </span>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Progress */}
      <Card>
        <CardContent className="pt-4">
          <h2 className="mb-3 font-medium">진행도</h2>
          <div className="flex items-center gap-3">
            <Progress value={piece.progressPercent} className="h-3 flex-1" />
            <span className="text-lg font-bold text-[#3F51B5]">
              {piece.progressPercent}%
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Memo */}
      {piece.memo && (
        <Card>
          <CardContent className="pt-4">
            <h2 className="mb-2 font-medium">메모</h2>
            <p className="text-sm text-[#616161]">{piece.memo}</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
