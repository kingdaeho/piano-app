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
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t bg-white/95 backdrop-blur-sm safe-area-bottom">
      <div className="mx-auto flex h-16 max-w-lg items-center justify-around">
        {tabs.map((tab) => {
          const isActive =
            pathname === tab.href || pathname.startsWith(tab.href + "/");
          const Icon = tab.icon;
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={cn(
                "flex flex-col items-center gap-0.5 px-3 py-2 text-xs transition-colors",
                isActive
                  ? "text-[#3F51B5] font-medium"
                  : "text-[#9E9E9E] hover:text-[#616161]"
              )}
            >
              <Icon
                className={cn("h-6 w-6", isActive && "fill-current")}
                strokeWidth={isActive ? 2.5 : 2}
              />
              <span>{tab.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
