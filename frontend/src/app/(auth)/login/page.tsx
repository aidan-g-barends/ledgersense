import { safeNext } from "@/lib/api";
import { LoginForm } from "@/components/auth/login-form";

// WHY a server page + client form: reading ?next= here (server side) avoids the
// useSearchParams() Suspense rule, and keeps the page itself light.
export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const { next } = await searchParams;
  return <LoginForm next={safeNext(next)} />;
}