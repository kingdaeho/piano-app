"use client";

import { useRouter } from "next/navigation";
import { ChevronLeft } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface TopBarProps {
  title: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
  className?: string;
}

export function TopBar({
  title,
  showBack = false,
  rightAction,
  className,
}: TopBarProps) {
  const router = useRouter();

  return (
    <header
      className={cn(
        "sticky top-0 z-40 flex h-14 items-center justify-between border-b border-border bg-[--nav-bg] px-4 backdrop-blur-sm",
        className
      )}
    >
      <div className="flex items-center gap-2">
        {showBack && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.back()}
            className="h-10 w-10 rounded-full"
          >
            <ChevronLeft className="h-6 w-6" />
          </Button>
        )}
      </div>
      <h1 className="absolute left-1/2 -translate-x-1/2 text-lg font-semibold text-foreground">
        {title}
      </h1>
      <div className="flex items-center gap-2">{rightAction}</div>
    </header>
  );
}
