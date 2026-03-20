import { BottomNav } from "@/components/layout/bottom-nav";
import { AuthGuard } from "@/components/auth/auth-guard";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard>
      <div className="mx-auto min-h-screen max-w-lg bg-background">
        <main className="pb-20">{children}</main>
        <BottomNav />
      </div>
    </AuthGuard>
  );
}
