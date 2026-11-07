"use client"; // error boundaries must be client components

import { RotateCcw, TriangleAlert } from "lucide-react";
import { btnSecondary } from "@/lib/ui";

export default function AppError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
      <TriangleAlert className="size-8 text-warning" aria-hidden="true" />
      <h1 className="text-lg font-semibold text-fg">Something went wrong on this page</h1>
      <p className="max-w-sm text-sm text-fg-muted">Your data is safe. Try again, and if it keeps happening, refresh the page.</p>
      {error.digest && <p className="font-mono text-xs text-fg-faint">Ref: {error.digest}</p>}
      {/* reset() re-renders the page without a full reload */}
      <button type="button" onClick={reset} className={btnSecondary}>
        <RotateCcw className="size-4" aria-hidden="true" />
        Try again
      </button>
    </div>
  );
}