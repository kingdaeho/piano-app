"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { X, Plus, Trash2 } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { PieceSelector } from "@/components/practice/piece-selector";
import { useCreateLessonNote } from "@/api/hooks";
import { toast } from "sonner";

export default function NewLessonNotePage() {
  const router = useRouter();
  const createNote = useCreateLessonNote();
  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [startTime, setStartTime] = useState("14:00");
  const [endTime, setEndTime] = useState("14:50");
  const [content, setContent] = useState("");
  const [feedback, setFeedback] = useState("");
  const [assignments, setAssignments] = useState<string[]>([""]);
  const [selectedPieces, setSelectedPieces] = useState<
    { id: number; title: string }[]
  >([]);
  const [showPieceSelector, setShowPieceSelector] = useState(false);

  const addAssignment = () => {
    setAssignments([...assignments, ""]);
  };

  const removeAssignment = (index: number) => {
    setAssignments(assignments.filter((_, i) => i !== index));
  };

  const updateAssignment = (index: number, value: string) => {
    const updated = [...assignments];
    updated[index] = value;
    setAssignments(updated);
  };

  const handleSave = async () => {
    try {
      await createNote.mutateAsync({
        lessonDate: date,
        startTime: startTime || undefined,
        endTime: endTime || undefined,
        content: content.trim() || undefined,
        teacherFeedback: feedback.trim() || undefined,
        pieceIds: selectedPieces.map((p) => p.id),
        assignments: assignments
          .filter((a) => a.trim())
          .map((a) => ({ content: a.trim() })),
      });
      toast.success("레슨 노트가 저장되었습니다");
      router.push("/lessons");
    } catch {
      toast.error("레슨 노트 저장에 실패했습니다");
    }
  };

  return (
    <div className="space-y-4 px-5 py-6">
      <div className="flex items-center justify-between">
        <button onClick={() => router.back()}>
          <X className="h-6 w-6" />
        </button>
        <h1 className="text-lg font-semibold">레슨 노트 작성</h1>
        <Button
          size="sm"
          onClick={handleSave}
          className="bg-[#3F51B5] hover:bg-[#283593]"
        >
          저장
        </Button>
      </div>

      {/* Date & Time */}
      <Card>
        <CardContent className="space-y-3 pt-4">
          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-3">
              <Label>날짜</Label>
              <Input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
            </div>
            <div className="col-span-1">
              <Label>시작</Label>
              <Input
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
              />
            </div>
            <div className="col-span-1">
              <Label>종료</Label>
              <Input
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Pieces */}
      <div>
        <Label className="mb-2 block">다룬 곡 (태그)</Label>
        <div className="flex flex-wrap items-center gap-2">
          {selectedPieces.map((p) => (
            <Badge
              key={p.id}
              className="bg-[#E8EAF6] text-[#1A237E]"
            >
              {p.title}
              <button
                onClick={() =>
                  setSelectedPieces(
                    selectedPieces.filter((sp) => sp.id !== p.id)
                  )
                }
                className="ml-1"
              >
                <X className="h-3 w-3" />
              </button>
            </Badge>
          ))}
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowPieceSelector(true)}
          >
            <Plus className="mr-1 h-4 w-4" />곡 추가
          </Button>
        </div>
      </div>

      {/* Content */}
      <div>
        <Label className="mb-2 block">레슨 내용</Label>
        <Textarea
          placeholder="오늘 레슨에서 배운 내용을 기록하세요..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="min-h-[120px]"
        />
      </div>

      {/* Feedback */}
      <div>
        <Label className="mb-2 block">선생님 피드백</Label>
        <Textarea
          placeholder="선생님이 지적하신 사항, 개선점..."
          value={feedback}
          onChange={(e) => setFeedback(e.target.value)}
          className="min-h-[100px]"
        />
      </div>

      {/* Assignments */}
      <div>
        <Label className="mb-2 block">다음 레슨까지 과제</Label>
        <div className="space-y-2">
          {assignments.map((a, i) => (
            <div key={i} className="flex items-center gap-2">
              <Input
                placeholder={`과제 ${i + 1}`}
                value={a}
                onChange={(e) => updateAssignment(i, e.target.value)}
              />
              {assignments.length > 1 && (
                <button onClick={() => removeAssignment(i)}>
                  <Trash2 className="h-4 w-4 text-[#F44336]" />
                </button>
              )}
            </div>
          ))}
          <Button variant="outline" size="sm" onClick={addAssignment}>
            <Plus className="mr-1 h-4 w-4" />
            과제 추가
          </Button>
        </div>
      </div>

      <Button
        onClick={handleSave}
        className="w-full bg-[#3F51B5] hover:bg-[#283593]"
        size="lg"
      >
        저장하기
      </Button>

      <PieceSelector
        open={showPieceSelector}
        onOpenChange={setShowPieceSelector}
        onSelect={(id, title) =>
          setSelectedPieces([...selectedPieces, { id, title }])
        }
      />
    </div>
  );
}
