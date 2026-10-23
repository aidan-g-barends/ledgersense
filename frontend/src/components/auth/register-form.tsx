"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Field } from "./field";

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

    // WHY check here too: instant feedback. The backend still enforces it (never trust the client).
    if (password.length < 12) {
      setError("Password must be at least 12 characters.");
      return;
    }

    setSubmitting(true);
    try {
      await api("/auth/register", { method: "POST", body: { businessName, email, password } });
      // Register doesn't log you in, so log in straight after with the same details
      await api("/auth/login", { method: "POST", body: { email, password } });
      router.replace("/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create your account. Please try again.");
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      <h1 className="text-xl font-semibold text-fg">Create your account</h1>

      <Field
        id="businessName"
        label="Business name"
        autoComplete="organization"
        value={businessName}
        onChange={setBusinessName}
      />
      <Field id="email" label="Email" type="email" autoComplete="email" value={email} onChange={setEmail} />
      <Field
        id="password"
        label="Password"
        type="password"
        autoComplete="new-password"
        value={password}
        onChange={setPassword}
        hint="At least 12 characters. A short sentence works well."
      />

      {error && (
        <p role="alert" className="text-sm text-critical">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="w-full rounded-md bg-primary px-4 py-2 text-sm font-medium text-canvas disabled:opacity-60"
      >
        {submitting ? "Creating account…" : "Create account"}
      </button>

      <p className="text-center text-sm text-fg/70">
        Already have an account?{" "}
        <Link href="/login" className="font-medium text-primary underline-offset-4 hover:underline">
          Log in
        </Link>
      </p>
    </form>
  );
}