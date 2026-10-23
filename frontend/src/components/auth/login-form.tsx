"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { api, ApiError, refreshSession } from "@/lib/api";
import { Field } from "./field";

export function LoginForm({ next }: { next: string }) {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [checking, setChecking] = useState(true);
  const triedRefresh = useRef(false);

  // NEW: the access cookie expired but the 7-day refresh cookie may still be valid.
  // Try a silent refresh first so returning users skip the form.
  useEffect(() => {
    // WHY the ref guard: React dev mode runs effects TWICE. Two refreshes would reuse
    // the same refresh token, and our reuse detection would log the user out everywhere.
    if (triedRefresh.current) return;
    triedRefresh.current = true;

    refreshSession()
      .then((ok) => (ok ? router.replace(next) : setChecking(false)))
      .catch(() => setChecking(false));
  }, [next, router]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault(); // stop the browser doing a full-page form post
    setError(null);
    setSubmitting(true);
    try {
      await api("/auth/login", { method: "POST", body: { email, password } });
      router.replace(next);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not log in. Please try again.");
      setSubmitting(false);
    }
  }

  if (checking) {
    return <p className="text-center text-sm text-fg/60">Checking your session…</p>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      <h1 className="text-xl font-semibold text-fg">Log in</h1>

      <Field id="email" label="Email" type="email" autoComplete="email" value={email} onChange={setEmail} />
      <Field
        id="password"
        label="Password"
        type="password"
        autoComplete="current-password"
        value={password}
        onChange={setPassword}
      />

      {/* role="alert" = screen readers announce the error immediately */}
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
        {submitting ? "Logging in…" : "Log in"}
      </button>

      <p className="text-center text-sm text-fg/70">
        New here?{" "}
        <Link href="/register" className="font-medium text-primary underline-offset-4 hover:underline">
          Create an account
        </Link>
      </p>
    </form>
  );
}