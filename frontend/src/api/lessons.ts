import { apiClient } from "./client";
import type { ApiResponse, LessonNote } from "@/types";

interface CreateLessonNoteRequest {
  lessonDate: string;
  startTime?: string;
  endTime?: string;
  content?: string;
  teacherFeedback?: string;
  pieceIds: number[];
  assignments: { content: string; isCompleted?: boolean }[];
}

interface UpdateLessonNoteRequest {
  lessonDate?: string;
  startTime?: string;
  endTime?: string;
  content?: string;
  teacherFeedback?: string;
  pieceIds?: number[];
  assignments?: { content: string; isCompleted?: boolean }[];
}

export const lessonsApi = {
  create: async (data: CreateLessonNoteRequest) => {
    const res = await apiClient.post<ApiResponse<LessonNote>>(
      "/lesson-notes",
      data,
    );
    return res.data.data;
  },

  getList: async (params?: { page?: number; size?: number }) => {
    const res = await apiClient.get<ApiResponse<LessonNote[]>>(
      "/lesson-notes",
      { params },
    );
    return { data: res.data.data, meta: res.data.meta };
  },

  getDetail: async (id: number) => {
    const res = await apiClient.get<ApiResponse<LessonNote>>(
      `/lesson-notes/${id}`,
    );
    return res.data.data;
  },

  update: async (id: number, data: UpdateLessonNoteRequest) => {
    const res = await apiClient.put<ApiResponse<LessonNote>>(
      `/lesson-notes/${id}`,
      data,
    );
    return res.data.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/lesson-notes/${id}`);
  },

  toggleAssignment: async (noteId: number, assignmentId: number) => {
    await apiClient.patch(
      `/lesson-notes/${noteId}/assignments/${assignmentId}`,
    );
  },
};
