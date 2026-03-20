"use client";

import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface StarRatingProps {
  value: number;
  max?: number;
  onChange?: (value: number) => void;
  size?: number;
  readonly?: boolean;
}

export function StarRating({
  value,
  max = 5,
  onChange,
  size = 20,
  readonly = false,
}: StarRatingProps) {
  return (
    <div className="flex gap-1">
      {Array.from({ length: max }, (_, i) => (
        <button
          key={i}
          type="button"
          disabled={readonly}
          onClick={() => onChange?.(i + 1)}
          className={cn("transition-colors", !readonly && "cursor-pointer hover:scale-110")}
        >
          <Star
            size={size}
            className={cn(
              i < value
                ? "fill-[#FFC107] text-[#FFC107]"
                : "fill-none text-[#E0E0E0]"
            )}
          />
        </button>
      ))}
    </div>
  );
}
