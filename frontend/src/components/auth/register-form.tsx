"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Field } from "./field";
import { FormError } from "./form-error";
import { SubmitButton } from "./submit-button";

export function RegisterForm() {
  const router = useRouter();
  const [businessName, setBusinessName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    // Instant feedback. The backend still enforces this (never trust the client).
    if (password.length < 12) {
      setError("Password must be at least 12 characters.");
      return;
    }

    setSubmitting(true);
    try {
      await api("/auth/register", { method: "POST", body: { businessName, email, password } });
      await api("/auth/login", { method: "POST", body: { email, password } });
      router.replace("/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create your account. Please try again.");
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5" noValidate>
      <Field
        id="businessName"
        label="Business name"
        autoComplete="organization"
        value={businessName}
        onChange={setBusinessName}
      />
      <Field id="email" label="Work email" type="email" autoComplete="email" value={email} onChange={setEmail} />
      <Field
        id="password"
        label="Password"
        autoComplete="new-password"
        value={password}
        onChange={setPassword}
        hint="At least 12 characters. A short sentence works well."
        revealable
      />

      <FormError message={error} />

      <SubmitButton busy={submitting} busyText="Creating your account…">
        Create account
      </SubmitButton>

      <p className="text-center text-sm text-fg-muted">
        Already have an account?{" "}
        <Link href="/login" className="font-medium text-primary underline-offset-4 hover:underline">
          Log in
        </Link>
      </p>
    </form>
  );
}