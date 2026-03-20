import { format, parseISO } from "date-fns";
import { ko } from "date-fns/locale";

export function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  }
  return `${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
}

export function formatDurationHM(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);

  if (hours > 0 && minutes > 0) {
    return `${hours}시간 ${minutes}분`;
  }
  if (hours > 0) {
    return `${hours}시간`;
  }
  return `${minutes}분`;
}

export function formatDurationTimer(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  return `${String(hours).padStart(1, "0")}:${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
}

export function formatDate(dateStr: string): string {
  return format(parseISO(dateStr), "M월 d일 (EEE)", { locale: ko });
}

export function formatDateFull(dateStr: string): string {
  return format(parseISO(dateStr), "yyyy.MM.dd (EEE)", { locale: ko });
}

export function formatDateShort(dateStr: string): string {
  return format(parseISO(dateStr), "MM.dd", { locale: ko });
}

export const PIECE_STATUS_LABEL: Record<string, string> = {
  NOT_STARTED: "시작 전",
  PRACTICING: "연습 중",
  FINISHING: "마무리",
  COMPLETED: "완성",
  ON_HOLD: "보류",
};

export const MOOD_EMOJI: Record<string, string> = {
  TERRIBLE: "😫",
  BAD: "😐",
  OK: "🙂",
  GOOD: "😊",
  GREAT: "🤩",
};

export const MOOD_LABEL: Record<string, string> = {
  TERRIBLE: "힘듦",
  BAD: "보통",
  OK: "괜찮음",
  GOOD: "좋음",
  GREAT: "최고!",
};

export function getDayOfWeek(dateStr: string): string {
  return format(parseISO(dateStr), "EEE", { locale: ko });
}
