"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { dashboardApi } from "./dashboard";
import { piecesApi } from "./pieces";
import { sessionsApi } from "./sessions";
import { lessonsApi } from "./lessons";
import { goalsApi } from "./goals";
import { usersApi } from "./users";
import type { PieceStatus, Mood } from "@/types";

// ===== Query Keys =====
export const queryKeys = {
  dashboard: ["dashboard"] as const,
  pieces: {
    all: ["pieces"] as const,
    list: (status?: PieceStatus) => ["pieces", { status }] as const,
    detail: (id: number) => ["pieces", id] as const,
  },
  sessions: {
    all: ["practice-sessions"] as const,
    list: () => ["practice-sessions"] as const,
    weeklyStats: (date?: string) => ["weekly-stats", date] as const,
  },
  lessons: {
    all: ["lesson-notes"] as const,
    list: () => ["lesson-notes"] as const,
    detail: (id: number) => ["lesson-notes", id] as const,
  },
  goals: ["goals"] as const,
  user: ["user", "me"] as const,
};

const tz = () => Intl.DateTimeFormat().resolvedOptions().timeZone;

// ===== Dashboard =====
export function useDashboard() {
  return useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: () => dashboardApi.get(tz()),
  });
}

// ===== Pieces =====
export function usePieces(status?: PieceStatus) {
  return useQuery({
    queryKey: queryKeys.pieces.list(status),
    queryFn: async () => {
      const result = await piecesApi.getList(
        status ? { status } : undefined,
      );
      return result.data;
    },
  });
}

export function usePiece(id: number) {
  return useQuery({
    queryKey: queryKeys.pieces.detail(id),
    queryFn: () => piecesApi.getDetail(id),
    enabled: id > 0,
  });
}

export function useCreatePiece() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: piecesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.pieces.all });
    },
  });
}

export function useUpdatePiece(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Parameters<typeof piecesApi.update>[1]) =>
      piecesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.pieces.all });
      queryClient.invalidateQueries({
        queryKey: queryKeys.pieces.detail(id),
      });
    },
  });
}

export function useDeletePiece() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: piecesApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.pieces.all });
    },
  });
}

// ===== Practice Sessions =====
export function usePracticeSessions() {
  return useQuery({
    queryKey: queryKeys.sessions.list(),
    queryFn: async () => {
      const result = await sessionsApi.getList();
      return result.data;
    },
  });
}

export function useWeeklyStats(date?: string) {
  return useQuery({
    queryKey: queryKeys.sessions.weeklyStats(date),
    queryFn: () => sessionsApi.getWeeklyStats({ date, timezone: tz() }),
  });
}

export function useStartSession() {
  return useMutation({ mutationFn: sessionsApi.start });
}

export function useSwitchPiece() {
  return useMutation({
    mutationFn: ({
      sessionId,
      pieceId,
    }: {
      sessionId: number;
      pieceId: number;
    }) => sessionsApi.switchPiece(sessionId, pieceId),
  });
}

export function useEndSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      sessionId,
      data,
    }: {
      sessionId: number;
      data?: { memo?: string; mood?: Mood };
    }) => sessionsApi.end(sessionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.sessions.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.goals });
    },
  });
}

// ===== Lesson Notes =====
export function useLessonNotes() {
  return useQuery({
    queryKey: queryKeys.lessons.list(),
    queryFn: async () => {
      const result = await lessonsApi.getList();
      return result.data;
    },
  });
}

export function useLessonNote(id: number) {
  return useQuery({
    queryKey: queryKeys.lessons.detail(id),
    queryFn: () => lessonsApi.getDetail(id),
    enabled: id > 0,
  });
}

export function useCreateLessonNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: lessonsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lessons.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

export function useUpdateLessonNote(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Parameters<typeof lessonsApi.update>[1]) =>
      lessonsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lessons.all });
      queryClient.invalidateQueries({
        queryKey: queryKeys.lessons.detail(id),
      });
    },
  });
}

export function useDeleteLessonNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: lessonsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lessons.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

export function useToggleAssignment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      noteId,
      assignmentId,
    }: {
      noteId: number;
      assignmentId: number;
    }) => lessonsApi.toggleAssignment(noteId, assignmentId),
    onSuccess: (_, { noteId }) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.lessons.detail(noteId),
      });
    },
  });
}

// ===== Goals =====
export function useGoals() {
  return useQuery({
    queryKey: queryKeys.goals,
    queryFn: () => goalsApi.get(tz()),
  });
}

export function useSetDailyGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: goalsApi.setDaily,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.goals });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

export function useSetWeeklyGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: goalsApi.setWeekly,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.goals });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}

// ===== User =====
export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.user,
    queryFn: usersApi.getMe,
  });
}
