"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { X } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { StarRating } from "@/components/common/star-rating";
import { useCreatePiece } from "@/api/hooks";
import { toast } from "sonner";
import type { PieceStatus } from "@/types";
import { PIECE_STATUS_LABEL } from "@/lib/format";
import { cn } from "@/lib/utils";

const statuses: PieceStatus[] = [
  "NOT_STARTED",
  "PRACTICING",
  "FINISHING",
  "COMPLETED",
  "ON_HOLD",
];

export default function NewPiecePage() {
  const router = useRouter();
  const createPiece = useCreatePiece();
  const [title, setTitle] = useState("");
  const [composer, setComposer] = useState("");
  const [genre, setGenre] = useState("");
  const [difficulty, setDifficulty] = useState(3);
  const [status, setStatus] = useState<PieceStatus>("PRACTICING");
  const [memo, setMemo] = useState("");

  const handleSave = async () => {
    if (!title.trim()) {
      toast.error("곡명을 입력해주세요");
      return;
    }
    try {
      await createPiece.mutateAsync({
        title: title.trim(),
        composer: composer.trim() || undefined,
        genre: genre.trim() || undefined,
        difficulty,
        status,
        memo: memo.trim() || undefined,
      });
      toast.success("곡이 등록되었습니다");
      router.push("/pieces");
    } catch {
      toast.error("곡 등록에 실패했습니다");
    }
  };

  return (
    <div className="space-y-4 px-5 py-6">
      <div className="flex items-center justify-between">
        <button onClick={() => router.back()}>
          <X className="h-6 w-6" />
        </button>
        <h1 className="text-lg font-semibold">곡 등록</h1>
        <Button
          size="sm"
          onClick={handleSave}
          className="bg-[#3F51B5] hover:bg-[#283593]"
        >
          저장
        </Button>
      </div>

      <div className="space-y-4">
        <div>
          <Label>곡명 *</Label>
          <Input
            placeholder="체르니 30번 - 8번 연습곡"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        <div>
          <Label>작곡가</Label>
          <Input
            placeholder="Carl Czerny"
            value={composer}
            onChange={(e) => setComposer(e.target.value)}
          />
        </div>

        <div>
          <Label>장르/교재</Label>
          <Input
            placeholder="클래식, 체르니, 연습곡"
            value={genre}
            onChange={(e) => setGenre(e.target.value)}
          />
        </div>

        <div>
          <Label className="mb-2 block">난이도 (체감)</Label>
          <StarRating value={difficulty} onChange={setDifficulty} size={28} />
        </div>

        <div>
          <Label className="mb-2 block">진행 상태</Label>
          <Card>
            <CardContent className="space-y-2 pt-4">
              {statuses.map((s) => (
                <button
                  key={s}
                  onClick={() => setStatus(s)}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm transition-colors",
                    status === s
                      ? "bg-[#E8EAF6] text-[#1A237E] font-medium"
                      : "hover:bg-gray-50"
                  )}
                >
                  <span
                    className={cn(
                      "h-4 w-4 rounded-full border-2",
                      status === s
                        ? "border-[#3F51B5] bg-[#3F51B5]"
                        : "border-[#E0E0E0]"
                    )}
                  />
                  {PIECE_STATUS_LABEL[s]}
                </button>
              ))}
            </CardContent>
          </Card>
        </div>

        <div>
          <Label>메모 (선택)</Label>
          <Textarea
            placeholder="이 곡에 대한 메모..."
            value={memo}
            onChange={(e) => setMemo(e.target.value)}
            className="min-h-[80px]"
          />
        </div>
      </div>

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
