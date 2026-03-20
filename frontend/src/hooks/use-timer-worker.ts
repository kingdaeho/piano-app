"use client";

import { useEffect, useRef } from "react";
import { useTimerStore } from "@/stores/timer-store";

export function useTimerWorker() {
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const isRunning = useTimerStore((s) => s.isRunning);
  const isPaused = useTimerStore((s) => s.isPaused);
  const tick = useTimerStore((s) => s.tick);

  useEffect(() => {
    if (isRunning && !isPaused) {
      intervalRef.current = setInterval(() => {
        tick();
      }, 1000);
    } else if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [isRunning, isPaused, tick]);
}
