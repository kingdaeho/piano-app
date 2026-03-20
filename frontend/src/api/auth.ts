import { apiClient } from "./client";
import type { ApiResponse, User } from "@/types";

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export const authApi = {
  signup: async (data: {
    email: string;
    password: string;
    name: string;
    experienceLevel?: string;
  }) => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/signup",
      data,
    );
    return res.data.data;
  },

  login: async (data: { email: string; password: string }) => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/login",
      data,
    );
    return res.data.data;
  },

  refresh: async (refreshToken: string) => {
    const res = await apiClient.post<ApiResponse<TokenResponse>>(
      "/auth/refresh",
      { refreshToken },
    );
    return res.data.data;
  },

  logout: async (refreshToken?: string) => {
    await apiClient.post(
      "/auth/logout",
      refreshToken ? { refreshToken } : undefined,
    );
  },
};
