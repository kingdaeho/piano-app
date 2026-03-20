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
        "flex flex-col items-center justify-center gap-4 py-20 text-center",
        className
      )}
    >
      {icon && (
        <div className="flex h-24 w-24 items-center justify-center rounded-full bg-accent/50 text-primary">
          {icon}
        </div>
      )}
      <div className="space-y-1.5">
        <p className="text-lg font-semibold text-foreground">{title}</p>
        {description && (
          <p className="mx-auto max-w-[260px] text-sm leading-relaxed text-muted-foreground">
            {description}
          </p>
        )}
      </div>
      {actionLabel && onAction && (
        <Button onClick={onAction} size="lg" className="btn-cta mt-2 rounded-full px-8">
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
