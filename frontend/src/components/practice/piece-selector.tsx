"use client";

import { useState } from "react";
import { Search, Plus, Check } from "lucide-react";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { usePieces } from "@/api/hooks";
import { PIECE_STATUS_LABEL } from "@/lib/format";
import { cn } from "@/lib/utils";

interface PieceSelectorProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedPieceId?: number | null;
  onSelect: (pieceId: number, pieceTitle: string) => void;
}

export function PieceSelector({
  open,
  onOpenChange,
  selectedPieceId,
  onSelect,
}: PieceSelectorProps) {
  const { data: pieces } = usePieces();
  const [search, setSearch] = useState("");

  const filtered = pieces?.filter(
    (p) =>
      p.title.toLowerCase().includes(search.toLowerCase()) ||
      p.composer?.toLowerCase().includes(search.toLowerCase())
  );

  const practicing = filtered?.filter(
    (p) => p.status === "PRACTICING" || p.status === "FINISHING"
  );
  const others = filtered?.filter(
    (p) => p.status !== "PRACTICING" && p.status !== "FINISHING"
  );

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="bottom" className="h-[80vh] rounded-t-2xl">
        <SheetHeader>
          <SheetTitle>곡 선택</SheetTitle>
        </SheetHeader>
        <div className="space-y-4 pt-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="곡명 검색..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10"
            />
          </div>

          <div className="max-h-[55vh] space-y-4 overflow-y-auto">
            {practicing && practicing.length > 0 && (
              <div>
                <p className="mb-2 text-sm font-medium text-muted-foreground">
                  연습 중인 곡
                </p>
                <div className="space-y-2">
                  {practicing.map((piece) => (
                    <button
                      key={piece.id}
                      onClick={() => {
                        onSelect(piece.id, piece.title);
                        onOpenChange(false);
                      }}
                      className={cn(
                        "flex w-full items-center justify-between rounded-lg border p-3 text-left transition-colors",
                        selectedPieceId === piece.id
                          ? "border-primary bg-accent"
                          : "hover:bg-muted"
                      )}
                    >
                      <div>
                        <p className="text-sm font-medium text-foreground">{piece.title}</p>
                        {piece.composer && (
                          <p className="text-xs text-muted-foreground">
                            {piece.composer}
                          </p>
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
                            {"★".repeat(piece.difficulty)}
                          </Badge>
                        )}
                        <Badge
                          variant="outline"
                          className="text-xs"
                        >
                          {PIECE_STATUS_LABEL[piece.status]}
                        </Badge>
                        {selectedPieceId === piece.id && (
                          <Check className="h-5 w-5 text-primary" />
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {others && others.length > 0 && (
              <div>
                <p className="mb-2 text-sm font-medium text-muted-foreground">
                  전체 곡 목록
                </p>
                <div className="space-y-2">
                  {others.map((piece) => (
                    <button
                      key={piece.id}
                      onClick={() => {
                        onSelect(piece.id, piece.title);
                        onOpenChange(false);
                      }}
                      className={cn(
                        "flex w-full items-center justify-between rounded-lg border p-3 text-left transition-colors",
                        selectedPieceId === piece.id
                          ? "border-primary bg-accent"
                          : "hover:bg-muted"
                      )}
                    >
                      <p className="text-sm font-medium text-foreground">{piece.title}</p>
                      <Badge
                        variant="outline"
                        className="text-xs"
                      >
                        {PIECE_STATUS_LABEL[piece.status]}
                      </Badge>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          <Button variant="outline" className="w-full">
            <Plus className="mr-2 h-4 w-4" />새 곡 등록
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
}
