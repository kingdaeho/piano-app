"use client";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
}

export function EmptyState({
  icon,
  title,
  description,
  actionLabel,
  onAction,
  className,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 py-16 text-center",
        className
      )}
    >
      {icon && <div className="text-4xl text-[#9E9E9E]">{icon}</div>}
      <p className="text-lg font-medium text-[#616161]">{title}</p>
      {description && (
        <p className="text-sm text-[#9E9E9E]">{description}</p>
      )}
      {actionLabel && onAction && (
        <Button
          onClick={onAction}
          className="mt-2 bg-[#3F51B5] hover:bg-[#283593]"
        >
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
