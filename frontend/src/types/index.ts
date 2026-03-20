// ===== Common =====
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    details?: string[];
  };
  meta?: PageMeta;
}

export interface PageMeta {
  total: number;
  page: number;
  size: number;
}

// ===== Auth =====
export interface User {
  id: number;
  email: string;
  name: string;
  experienceLevel: ExperienceLevel;
  profileImageUrl?: string;
  dailyGoalMinutes: number;
  weeklyGoalDays: number;
  weeklyGoalMinutes: number;
}

export type ExperienceLevel = "BEGINNER" | "LESSON_STUDENT" | "RETURNER";

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  user: User;
}

// ===== Practice =====
export type Mood = "GREAT" | "GOOD" | "OK" | "BAD" | "TERRIBLE";

export interface PracticeSession {
  id: number;
  startedAt: string;
  endedAt?: string;
  totalDurationSeconds: number;
  memo?: string;
  mood?: Mood;
  pieces: PracticeSessionPiece[];
}

export interface PracticeSessionPiece {
  pieceId: number;
  title: string;
  durationSeconds: number;
  orderIndex?: number;
}

export interface GoalProgress {
  targetMinutes: number;
  achievedMinutes: number;
  percent: number;
}

// ===== Piece =====
export type PieceStatus =
  | "NOT_STARTED"
  | "PRACTICING"
  | "FINISHING"
  | "COMPLETED"
  | "ON_HOLD";

export interface Piece {
  id: number;
  title: string;
  composer?: string;
  genre?: string;
  difficulty?: number;
  status: PieceStatus;
  progressPercent: number;
  memo?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
}

export interface PieceSummary {
  id: number;
  title: string;
  composer?: string;
  difficulty?: number;
  status: PieceStatus;
  progressPercent: number;
}

// ===== Lesson Note =====
export interface LessonNote {
  id: number;
  lessonNumber: number;
  lessonDate: string;
  startTime?: string;
  endTime?: string;
  content?: string;
  teacherFeedback?: string;
  pieces: PieceSummary[];
  assignments: Assignment[];
  createdAt: string;
}

export interface Assignment {
  id: number;
  content: string;
  isCompleted: boolean;
  orderIndex: number;
}

// ===== Goals =====
export interface Goals {
  daily: GoalProgress;
  weekly: {
    targetDays: number;
    achievedDays: number;
    targetMinutes: number;
    achievedMinutes: number;
  };
  streak: {
    currentDays: number;
    longestDays: number;
  };
  pieceGoals: PieceGoal[];
}

export interface PieceGoal {
  id: number;
  piece: PieceSummary;
  targetDate?: string;
  currentProgressPercent: number;
  daysRemaining?: number;
}

// ===== Dashboard =====
export interface Dashboard {
  today: {
    goalMinutes: number;
    achievedMinutes: number;
    percent: number;
  };
  streak: {
    currentDays: number;
    weeklyAchievedDays: number;
    weeklyTargetDays: number;
  };
  latestLessonNote?: LessonNote;
  activePieces: PieceSummary[];
  weeklyChart: { date: string; durationSeconds: number }[];
}

// ===== Stats =====
export interface WeeklyStats {
  weekStart: string;
  weekEnd: string;
  totalDurationSeconds: number;
  practiceDays: number;
  dailyStats: { date: string; durationSeconds: number }[];
  pieceStats: {
    pieceId: number;
    title: string;
    durationSeconds: number;
    percent: number;
  }[];
  previousWeekDurationSeconds: number;
  changePercent: number;
}
