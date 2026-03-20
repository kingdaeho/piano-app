"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, Search, BookOpen } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/common/empty-state";
import { useLessonNotes } from "@/api/hooks";
import { formatDateFull } from "@/lib/format";

type Filter = "all" | "feedback" | "incomplete";

export default function LessonsPage() {
  const { data: notes, isLoading } = useLessonNotes();
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<Filter>("all");

  if (isLoading || !notes) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }

  const filtered = notes.filter((note) => {
    const matchesSearch =
      search === "" ||
      note.content?.toLowerCase().includes(search.toLowerCase()) ||
      note.pieces.some((p) =>
        p.title.toLowerCase().includes(search.toLowerCase())
      );
    if (filter === "feedback") return matchesSearch && note.teacherFeedback;
    if (filter === "incomplete")
      return (
        matchesSearch && note.assignments.some((a) => !a.isCompleted)
      );
    return matchesSearch;
  });

  const filters: { key: Filter; label: string }[] = [
    { key: "all", label: "전체" },
    { key: "feedback", label: "피드백만" },
    { key: "incomplete", label: "미완료 과제" },
  ];

  return (
    <div className="space-y-5 px-5 pb-8 pt-7">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold tracking-tight text-foreground">레슨 노트</h1>
        <Button
          onClick={() => router.push("/lessons/new")}
          size="sm"
        >
          <Plus className="mr-1 h-4 w-4" />
          작성
        </Button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="레슨 내용, 곡명으로 검색..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10"
        />
      </div>

      {/* Filters */}
      <div className="flex gap-2">
        {filters.map((f) => (
          <Button
            key={f.key}
            variant={filter === f.key ? "default" : "outline"}
            size="sm"
            onClick={() => setFilter(f.key)}
          >
            {f.label}
          </Button>
        ))}
      </div>

      <p className="text-sm text-muted-foreground">
        총 {notes.length}회 레슨
      </p>

      {/* Notes List */}
      {filtered.length === 0 ? (
        <EmptyState
          icon={<BookOpen className="h-8 w-8" />}
          title="레슨 노트가 없습니다"
          description="첫 레슨 노트를 작성해보세요"
          actionLabel="노트 작성"
          onAction={() => router.push("/lessons/new")}
        />
      ) : (
        <div className="space-y-3">
          {filtered.map((note) => {
            const completedCount = note.assignments.filter(
              (a) => a.isCompleted
            ).length;
            return (
              <Card
                key={note.id}
                className="card-elevated cursor-pointer border-l-4 border-l-primary shadow-sm transition-shadow hover:shadow-md"
                onClick={() => router.push(`/lessons/${note.id}`)}
              >
                <CardContent className="space-y-2.5 px-5 pb-4 pt-4">
                  <div className="flex items-center justify-between">
                    <Badge className="rounded-full bg-primary px-2.5 py-0.5 text-xs font-semibold text-primary-foreground">
                      {note.lessonNumber}회차
                    </Badge>
                    <span className="text-xs text-muted-foreground">
                      {formatDateFull(note.lessonDate)}
                    </span>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {note.pieces.map((p) => (
                      <Badge
                        key={p.id}
                        variant="secondary"
                        className="bg-accent text-xs text-accent-foreground"
                      >
                        {p.title}
                      </Badge>
                    ))}
                  </div>
                  {note.content && (
                    <p className="line-clamp-1 text-sm text-muted-foreground">
                      {note.content.split("\n")[0]}
                    </p>
                  )}
                  {note.assignments.length > 0 && (
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <span>
                        과제 {completedCount}/{note.assignments.length} 완료
                      </span>
                      <div className="flex gap-0.5">
                        {note.assignments.map((a) => (
                          <span key={a.id}>
                            {a.isCompleted ? "✓" : "☐"}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
