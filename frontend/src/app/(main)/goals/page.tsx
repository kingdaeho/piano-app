"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { useGoals, useSetDailyGoal, useSetWeeklyGoal } from "@/api/hooks";
import { toast } from "sonner";
import { cn } from "@/lib/utils";

const dailyOptions = [30, 45, 60, 90, 120];
const weeklyDayOptions = [3, 4, 5, 6, 7];
const weeklyTimeOptions = [180, 300, 420]; // 3h, 5h, 7h

export default function GoalSettingsPage() {
  const router = useRouter();
  const { data: goals } = useGoals();
  const setDailyGoalMutation = useSetDailyGoal();
  const setWeeklyGoalMutation = useSetWeeklyGoal();
  const [dailyGoal, setDailyGoal] = useState(60);
  const [weeklyDays, setWeeklyDays] = useState(5);
  const [weeklyMinutes, setWeeklyMinutes] = useState(300);

  useEffect(() => {
    if (goals) {
      setDailyGoal(goals.daily.targetMinutes);
      setWeeklyDays(goals.weekly.targetDays);
      setWeeklyMinutes(goals.weekly.targetMinutes);
    }
  }, [goals]);

  const handleSave = async () => {
    try {
      await Promise.all([
        setDailyGoalMutation.mutateAsync(dailyGoal),
        setWeeklyGoalMutation.mutateAsync({
          targetDays: weeklyDays,
          targetMinutes: weeklyMinutes,
        }),
      ]);
      toast.success("목표가 저장되었습니다");
      router.back();
    } catch {
      toast.error("목표 저장에 실패했습니다");
    }
  };

  return (
    <div className="space-y-6 px-5 py-6">
      <div className="flex items-center gap-3">
        <button onClick={() => router.back()}>
          <ChevronLeft className="h-6 w-6" />
        </button>
        <h1 className="text-xl font-bold">목표 설정</h1>
      </div>

      {/* Daily Goal */}
      <Card>
        <CardContent className="pt-4">
          <Label className="text-base font-medium">하루 연습 목표 시간</Label>
          <div className="mt-3 flex flex-wrap gap-2">
            {dailyOptions.map((mins) => (
              <Button
                key={mins}
                variant={dailyGoal === mins ? "default" : "outline"}
                size="sm"
                onClick={() => setDailyGoal(mins)}
                className={cn(
                  "min-w-[60px]",
                  dailyGoal === mins && "bg-[#3F51B5]"
                )}
              >
                {mins}분
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Weekly Days */}
      <Card>
        <CardContent className="pt-4">
          <Label className="text-base font-medium">
            주간 연습 일수 목표
          </Label>
          <div className="mt-3 flex flex-wrap gap-2">
            {weeklyDayOptions.map((days) => (
              <Button
                key={days}
                variant={weeklyDays === days ? "default" : "outline"}
                size="sm"
                onClick={() => setWeeklyDays(days)}
                className={cn(
                  "min-w-[60px]",
                  weeklyDays === days && "bg-[#3F51B5]"
                )}
              >
                {days}일
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Weekly Time */}
      <Card>
        <CardContent className="pt-4">
          <Label className="text-base font-medium">
            주간 연습 시간 목표
          </Label>
          <div className="mt-3 flex flex-wrap gap-2">
            {weeklyTimeOptions.map((mins) => (
              <Button
                key={mins}
                variant={weeklyMinutes === mins ? "default" : "outline"}
                size="sm"
                onClick={() => setWeeklyMinutes(mins)}
                className={cn(
                  "min-w-[80px]",
                  weeklyMinutes === mins && "bg-[#3F51B5]"
                )}
              >
                {mins / 60}시간
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

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
