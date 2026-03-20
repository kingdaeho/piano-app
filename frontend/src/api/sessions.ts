import { apiClient } from "./client";
import type { ApiResponse, Mood, PracticeSession, WeeklyStats } from "@/types";

export const sessionsApi = {
  start: async (pieceId?: number) => {
    const res = await apiClient.post<ApiResponse<PracticeSession>>(
      "/practice-sessions",
      pieceId ? { pieceId } : undefined,
    );
    return res.data.data;
  },

  switchPiece: async (sessionId: number, pieceId: number) => {
    const res = await apiClient.patch<ApiResponse<PracticeSession>>(
      `/practice-sessions/${sessionId}/switch-piece`,
      { pieceId },
    );
    return res.data.data;
  },

  end: async (
    sessionId: number,
    data?: { memo?: string; mood?: Mood },
  ) => {
    const res = await apiClient.patch<ApiResponse<PracticeSession>>(
      `/practice-sessions/${sessionId}/end`,
      data ?? {},
    );
    return res.data.data;
  },

  getList: async (params?: { page?: number; size?: number }) => {
    const res = await apiClient.get<ApiResponse<PracticeSession[]>>(
      "/practice-sessions",
      { params },
    );
    return { data: res.data.data, meta: res.data.meta };
  },

  getDetail: async (id: number) => {
    const res = await apiClient.get<ApiResponse<PracticeSession>>(
      `/practice-sessions/${id}`,
    );
    return res.data.data;
  },

  getWeeklyStats: async (params?: {
    date?: string;
    timezone?: string;
  }) => {
    const res = await apiClient.get<ApiResponse<WeeklyStats>>(
      "/practice-sessions/stats/weekly",
      { params },
    );
    return res.data.data;
  },
};
