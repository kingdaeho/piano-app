"use client";

import { useRouter } from "next/navigation";
import { ChevronLeft } from "lucide-react";
import { cn } from "@/lib/utils";

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
        "sticky top-0 z-40 flex h-14 items-center justify-between border-b bg-white/95 px-4 backdrop-blur-sm",
        className
      )}
    >
      <div className="flex items-center gap-2">
        {showBack && (
          <button
            onClick={() => router.back()}
            className="flex h-10 w-10 items-center justify-center rounded-full hover:bg-gray-100"
          >
            <ChevronLeft className="h-6 w-6" />
          </button>
        )}
      </div>
      <h1 className="absolute left-1/2 -translate-x-1/2 text-lg font-semibold">
        {title}
      </h1>
      <div className="flex items-center gap-2">{rightAction}</div>
    </header>
  );
}
