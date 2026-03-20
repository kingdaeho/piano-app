"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Home, Timer, BookOpen, Music, BarChart3 } from "lucide-react";
import { cn } from "@/lib/utils";

const tabs = [
  { href: "/home", label: "홈", icon: Home },
  { href: "/practice", label: "연습", icon: Timer },
  { href: "/lessons", label: "레슨", icon: BookOpen },
  { href: "/pieces", label: "곡관리", icon: Music },
  { href: "/stats", label: "통계", icon: BarChart3 },
] as const;

export function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-border bg-[--nav-bg] backdrop-blur-sm safe-area-bottom">
      <div className="mx-auto flex h-16 max-w-lg items-center justify-around px-2">
        {tabs.map((tab) => {
          const isActive =
            pathname === tab.href || pathname.startsWith(tab.href + "/");
          const Icon = tab.icon;
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={cn(
                "relative flex flex-col items-center gap-0.5 rounded-xl px-3 py-1.5 text-[11px] transition-all",
                isActive
                  ? "text-primary font-semibold"
                  : "text-[--nav-inactive] hover:text-[--nav-inactive-hover]"
              )}
            >
              {isActive && (
                <span className="absolute -top-1.5 h-0.5 w-5 rounded-full bg-primary" />
              )}
              <div
                className={cn(
                  "flex h-8 w-8 items-center justify-center rounded-full transition-all",
                  isActive && "bg-accent scale-110"
                )}
              >
                <Icon
                  className={cn("h-5 w-5", isActive && "fill-current")}
                  strokeWidth={isActive ? 2.5 : 1.8}
                />
              </div>
              <span>{tab.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
