import { apiClient } from "./client";
import type { ApiResponse, Piece, PieceStatus } from "@/types";

interface CreatePieceRequest {
  title: string;
  composer?: string;
  genre?: string;
  difficulty?: number;
  status?: PieceStatus;
  memo?: string;
}

interface UpdatePieceRequest {
  title?: string;
  composer?: string;
  genre?: string;
  difficulty?: number;
  status?: PieceStatus;
  progressPercent?: number;
  memo?: string;
}

export const piecesApi = {
  create: async (data: CreatePieceRequest) => {
    const res = await apiClient.post<ApiResponse<Piece>>("/pieces", data);
    return res.data.data;
  },

  getList: async (params?: {
    status?: PieceStatus;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<ApiResponse<Piece[]>>("/pieces", {
      params,
    });
    return { data: res.data.data, meta: res.data.meta };
  },

  getDetail: async (id: number) => {
    const res = await apiClient.get<ApiResponse<Piece>>(`/pieces/${id}`);
    return res.data.data;
  },

  update: async (id: number, data: UpdatePieceRequest) => {
    const res = await apiClient.put<ApiResponse<Piece>>(
      `/pieces/${id}`,
      data,
    );
    return res.data.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/pieces/${id}`);
  },
};
