import { apiClient } from "./client";
import type { ApiResponse, Dashboard } from "@/types";

export const dashboardApi = {
  get: async (timezone?: string) => {
    const res = await apiClient.get<ApiResponse<Dashboard>>("/dashboard", {
      params: { timezone },
    });
    return res.data.data;
  },
};
