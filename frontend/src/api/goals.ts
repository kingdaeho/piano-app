import { apiClient } from "./client";
import type { ApiResponse, Goals } from "@/types";

export const goalsApi = {
  get: async (timezone?: string) => {
    const res = await apiClient.get<ApiResponse<Goals>>("/goals", {
      params: { timezone },
    });
    return res.data.data;
  },

  setDaily: async (targetMinutes: number) => {
    await apiClient.put("/goals/daily", { targetMinutes });
  },

  setWeekly: async (data: {
    targetDays?: number;
    targetMinutes?: number;
  }) => {
    await apiClient.put("/goals/weekly", data);
  },
};
