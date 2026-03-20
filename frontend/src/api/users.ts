import { apiClient } from "./client";
import type { ApiResponse, User } from "@/types";

export const usersApi = {
  getMe: async () => {
    const res = await apiClient.get<ApiResponse<User>>("/users/me");
    return res.data.data;
  },

  updateMe: async (
    data: Partial<
      Pick<
        User,
        | "name"
        | "profileImageUrl"
        | "experienceLevel"
        | "dailyGoalMinutes"
        | "weeklyGoalDays"
        | "weeklyGoalMinutes"
      >
    >,
  ) => {
    const res = await apiClient.patch<ApiResponse<User>>("/users/me", data);
    return res.data.data;
  },
};
