import { create } from "zustand";
import type { Mood, PracticeSessionPiece } from "@/types";

interface TimerState {
  isRunning: boolean;
  isPaused: boolean;
  elapsedSeconds: number;
  currentPieceId: number | null;
  currentPieceTitle: string;
  currentPieceStartTime: number;
  practicedPieces: PracticeSessionPiece[];
  memo: string;
  mood: Mood | null;
  sessionId: number | null;

  start: (pieceId: number, pieceTitle: string) => void;
  pause: () => void;
  resume: () => void;
  tick: () => void;
  switchPiece: (pieceId: number, pieceTitle: string) => void;
  setMemo: (memo: string) => void;
  setMood: (mood: Mood) => void;
  setSessionId: (id: number) => void;
  stop: () => void;
  reset: () => void;
}

export const useTimerStore = create<TimerState>((set, get) => ({
  isRunning: false,
  isPaused: false,
  elapsedSeconds: 0,
  currentPieceId: null,
  currentPieceTitle: "",
  currentPieceStartTime: 0,
  practicedPieces: [],
  memo: "",
  mood: null,
  sessionId: null,

  start: (pieceId, pieceTitle) =>
    set({
      isRunning: true,
      isPaused: false,
      elapsedSeconds: 0,
      currentPieceId: pieceId,
      currentPieceTitle: pieceTitle,
      currentPieceStartTime: 0,
      practicedPieces: [],
      memo: "",
      mood: null,
    }),

  pause: () => set({ isPaused: true }),

  resume: () => set({ isPaused: false }),

  tick: () => {
    const state = get();
    if (state.isRunning && !state.isPaused) {
      set({
        elapsedSeconds: state.elapsedSeconds + 1,
        currentPieceStartTime: state.currentPieceStartTime + 1,
      });
    }
  },

  switchPiece: (pieceId, pieceTitle) => {
    const state = get();
    if (state.currentPieceId !== null) {
      const existingIdx = state.practicedPieces.findIndex(
        (p) => p.pieceId === state.currentPieceId
      );
      const updatedPieces = [...state.practicedPieces];
      if (existingIdx >= 0) {
        updatedPieces[existingIdx] = {
          ...updatedPieces[existingIdx],
          durationSeconds:
            updatedPieces[existingIdx].durationSeconds +
            state.currentPieceStartTime,
        };
      } else {
        updatedPieces.push({
          pieceId: state.currentPieceId,
          title: state.currentPieceTitle,
          durationSeconds: state.currentPieceStartTime,
        });
      }
      set({
        currentPieceId: pieceId,
        currentPieceTitle: pieceTitle,
        currentPieceStartTime: 0,
        practicedPieces: updatedPieces,
      });
    }
  },

  setMemo: (memo) => set({ memo }),
  setMood: (mood) => set({ mood }),
  setSessionId: (id) => set({ sessionId: id }),

  stop: () => {
    const state = get();
    const finalPieces = [...state.practicedPieces];
    if (state.currentPieceId !== null) {
      const existingIdx = finalPieces.findIndex(
        (p) => p.pieceId === state.currentPieceId
      );
      if (existingIdx >= 0) {
        finalPieces[existingIdx] = {
          ...finalPieces[existingIdx],
          durationSeconds:
            finalPieces[existingIdx].durationSeconds +
            state.currentPieceStartTime,
        };
      } else {
        finalPieces.push({
          pieceId: state.currentPieceId,
          title: state.currentPieceTitle,
          durationSeconds: state.currentPieceStartTime,
        });
      }
    }
    set({
      isRunning: false,
      isPaused: false,
      practicedPieces: finalPieces,
    });
  },

  reset: () =>
    set({
      isRunning: false,
      isPaused: false,
      elapsedSeconds: 0,
      currentPieceId: null,
      currentPieceTitle: "",
      currentPieceStartTime: 0,
      practicedPieces: [],
      memo: "",
      mood: null,
      sessionId: null,
    }),
}));
