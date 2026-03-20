export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-[#E8EAF6] px-5">
      <div className="w-full max-w-md">{children}</div>
    </div>
  );
}
