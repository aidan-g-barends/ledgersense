"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { LoaderCircle } from "lucide-react";
import { api, ApiError, refreshSession } from "@/lib/api";
import { Field } from "./field";
import { FormError } from "./form-error";
import { SubmitButton } from "./submit-button";

export function LoginForm({ next }: { next: string }) {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [checking, setChecking] = useState(true);
  const triedRefresh = useRef(false);

  // Silent refresh: skip the form if the 7-day refresh cookie is still valid
  useEffect(() => {
    // WHY the ref guard: React dev mode runs effects twice. Two refreshes = token reuse = forced logout.
    if (triedRefresh.current) return;
    triedRefresh.current = true;

    refreshSession()
      .then((ok) => (ok ? router.replace(next) : setChecking(false)))
      .catch(() => setChecking(false));
  }, [next, router]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
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
    return (
      <p className="flex items-center gap-2 text-sm text-fg-muted">
        <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
        Checking your session…
      </p>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5" noValidate>
      <Field id="email" label="Email" type="email" autoComplete="email" value={email} onChange={setEmail} />
      <Field
        id="password"
        label="Password"
        autoComplete="current-password"
        value={password}
        onChange={setPassword}
        revealable
      />

      <FormError message={error} />

      <SubmitButton busy={submitting} busyText="Logging in…">
        Log in
      </SubmitButton>

      <p className="text-center text-sm text-fg-muted">
        New to LedgerSense?{" "}
        <Link href="/register" className="font-medium text-primary underline-offset-4 hover:underline">
          Create an account
        </Link>
      </p>
    </form>
  );
}