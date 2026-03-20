"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft, ChevronRight, Pencil } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Separator } from "@/components/ui/separator";
import { useLessonNote, useLessonNotes, useToggleAssignment } from "@/api/hooks";
import { formatDateFull } from "@/lib/format";

export default function LessonNoteDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const noteId = parseInt(id, 10);
  const { data: note, isLoading } = useLessonNote(noteId);
  const { data: allNotes } = useLessonNotes();
  const toggleAssignment = useToggleAssignment();
  const router = useRouter();

  if (isLoading || !note) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#3F51B5] border-t-transparent" />
      </div>
    );
  }

  const currentIndex = allNotes?.findIndex((n) => n.id === noteId) ?? -1;
  const prevNote = allNotes && currentIndex < allNotes.length - 1
    ? allNotes[currentIndex + 1]
    : null;
  const nextNote = allNotes && currentIndex > 0
    ? allNotes[currentIndex - 1]
    : null;

  return (
    <div className="space-y-4 px-5 py-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button onClick={() => router.back()}>
          <ChevronLeft className="h-6 w-6" />
        </button>
        <h1 className="text-lg font-semibold">
          {note.lessonNumber}회차 레슨 노트
        </h1>
        <button className="text-[#3F51B5]">
          <Pencil className="h-5 w-5" />
        </button>
      </div>

      {/* Date */}
      <p className="text-sm text-[#616161]">
        {formatDateFull(note.lessonDate)}
        {note.startTime && note.endTime && (
          <span>
            {" "}
            &middot; {note.startTime}~{note.endTime}
          </span>
        )}
      </p>

      {/* Pieces */}
      <div className="flex flex-wrap gap-1">
        {note.pieces.map((p) => (
          <Badge
            key={p.id}
            className="bg-[#E8EAF6] text-[#1A237E]"
          >
            {p.title}
          </Badge>
        ))}
      </div>

      {/* Content */}
      {note.content && (
        <Card>
          <CardContent className="pt-4">
            <h2 className="mb-2 font-medium">레슨 내용</h2>
            <Separator className="mb-3" />
            <div className="whitespace-pre-wrap text-sm text-[#212121]">
              {note.content}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Feedback */}
      {note.teacherFeedback && (
        <Card>
          <CardContent className="pt-4">
            <h2 className="mb-2 font-medium">선생님 피드백</h2>
            <Separator className="mb-3" />
            <div className="whitespace-pre-wrap text-sm text-[#212121]">
              {note.teacherFeedback}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Assignments */}
      {note.assignments.length > 0 && (
        <Card>
          <CardContent className="pt-4">
            <h2 className="mb-2 font-medium">과제</h2>
            <Separator className="mb-3" />
            <div className="space-y-3">
              {note.assignments.map((a) => (
                <div key={a.id} className="flex items-start gap-3">
                  <Checkbox
                    checked={a.isCompleted}
                    onCheckedChange={() =>
                      toggleAssignment.mutate({
                        noteId,
                        assignmentId: a.id,
                      })
                    }
                  />
                  <span
                    className={`text-sm ${a.isCompleted ? "text-[#9E9E9E] line-through" : "text-[#212121]"}`}
                  >
                    {a.content}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Navigation */}
      <div className="flex items-center justify-between pt-4">
        {prevNote ? (
          <button
            onClick={() => router.push(`/lessons/${prevNote.id}`)}
            className="flex items-center text-sm text-[#3F51B5]"
          >
            <ChevronLeft className="h-4 w-4" />
            {prevNote.lessonNumber}회차
          </button>
        ) : (
          <div />
        )}
        {nextNote ? (
          <button
            onClick={() => router.push(`/lessons/${nextNote.id}`)}
            className="flex items-center text-sm text-[#3F51B5]"
          >
            {nextNote.lessonNumber}회차
            <ChevronRight className="h-4 w-4" />
          </button>
        ) : (
          <div />
        )}
      </div>
    </div>
  );
}
