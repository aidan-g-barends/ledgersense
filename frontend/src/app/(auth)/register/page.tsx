import { AuthShell } from "@/components/auth/auth-shell";
import { RegisterForm } from "@/components/auth/register-form";

export default function RegisterPage() {
  return (
    <AuthShell title="Create your account" subtitle="Set up your business in under a minute.">
      <RegisterForm />
    </AuthShell>
  );
}
