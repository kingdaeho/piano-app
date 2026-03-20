"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/auth-store";
import { authApi } from "@/api/auth";
import { toast } from "sonner";

export default function SignupPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim() || !password.trim()) {
      toast.error("모든 필드를 입력해주세요");
      return;
    }
    if (password.length < 8) {
      toast.error("비밀번호는 8자 이상이어야 합니다");
      return;
    }
    setIsSubmitting(true);
    try {
      const res = await authApi.signup({ email, password, name });
      login(res.user, res.accessToken, res.refreshToken);
      router.push("/home");
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { error?: { message?: string } } } })
          ?.response?.data?.error?.message ?? "회원가입에 실패했습니다";
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card className="shadow-lg">
      <CardHeader className="items-center space-y-2 pb-2">
        <div className="text-4xl">🎹</div>
        <h1 className="text-2xl font-bold text-[#1A237E]">회원가입</h1>
        <p className="text-sm text-[#616161]">피아노 연습을 시작해보세요</p>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSignup} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">이름</Label>
            <Input
              id="name"
              placeholder="이름을 입력하세요"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">이메일</Label>
            <Input
              id="email"
              type="email"
              placeholder="email@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">비밀번호</Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <Button
            type="submit"
            className="w-full bg-[#3F51B5] hover:bg-[#283593]"
            disabled={isSubmitting}
          >
            {isSubmitting ? "가입 중..." : "가입하기"}
          </Button>
        </form>
        <div className="mt-4 text-center text-sm text-[#616161]">
          이미 계정이 있으신가요?{" "}
          <Link href="/login" className="font-medium text-[#3F51B5]">
            로그인
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
