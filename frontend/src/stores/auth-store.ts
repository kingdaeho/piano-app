import { create } from "zustand";
import type { User } from "@/types";
import { usersApi } from "@/api/users";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: User, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
  hydrate: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  login: (user, accessToken, refreshToken) => {
    if (typeof window !== "undefined") {
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
    }
    set({ user, isAuthenticated: true, isLoading: false });
  },
  logout: () => {
    if (typeof window !== "undefined") {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
    }
    set({ user: null, isAuthenticated: false, isLoading: false });
  },
  updateUser: (updates) =>
    set((state) => ({
      user: state.user ? { ...state.user, ...updates } : null,
    })),
  hydrate: async () => {
    if (typeof window === "undefined") return;
    const token = localStorage.getItem("accessToken");
    if (!token) {
      set({ isLoading: false });
      return;
    }
    try {
      const user = await usersApi.getMe();
      set({ user, isAuthenticated: true, isLoading: false });
    } catch {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },
}));
