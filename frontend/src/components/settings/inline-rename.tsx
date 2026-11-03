"use client";

import { useState } from "react";
import { Check, Pencil, X } from "lucide-react";
import { ApiError } from "@/lib/api";

type Props = {
  value: string;
  label: string;                                // used in aria-labels, e.g. "FNB Cheque"
  maxLength: number;
  onSave: (name: string) => Promise<void>;      // parent does the API call; throw = show error
};

// NEW: reusable "click pencil -> edit -> save/cancel". Knows nothing about WHAT it renames.
export function InlineRename({ value, label, maxLength, onSave }: Props) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function start() {
    setDraft(value); // always start from the current saved name
    setError(null);
    setEditing(true);
  }

  function cancel() {
    setEditing(false);
    setError(null);
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    const name = draft.trim();
    // WHY: nothing changed (or emptied it)? Just close, no pointless API call.
    if (!name || name === value) {
      cancel();
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await onSave(name);
      setEditing(false);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not rename.");
    } finally {
      setBusy(false);
    }
  }

  if (!editing) {
    return (
      <div className="group flex min-w-0 items-center gap-1.5">
        <span className="truncate text-sm font-medium text-fg">{value}</span>
        <button
          type="button"
          onClick={start}
          aria-label={`Rename ${label}`}
          className="rounded p-1 text-fg-faint opacity-60 transition hover:bg-raised hover:text-fg focus-visible:opacity-100 group-hover:opacity-100"
        >
          <Pencil className="size-3.5" aria-hidden="true" />
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSave} className="min-w-0 flex-1 space-y-1">
      <div className="flex items-center gap-1">
        <input
          autoFocus
          aria-label={`New name for ${label}`}
          value={draft}
          maxLength={maxLength}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Escape") cancel(); // Esc = cancel, Enter = submit (free with <form>)
          }}
          className="h-8 min-w-0 flex-1 rounded border border-line bg-surface px-2 text-sm text-fg outline-none focus:border-primary focus:ring-1 focus:ring-primary"
        />
        <button
          type="submit"
          disabled={busy}
          aria-label="Save name"
          className="rounded p-1.5 text-positive transition-colors hover:bg-positive/10 disabled:opacity-60"
        >
          <Check className="size-4" aria-hidden="true" />
        </button>
        <button
          type="button"
          onClick={cancel}
          aria-label="Cancel rename"
          className="rounded p-1.5 text-fg-faint transition-colors hover:bg-raised"
        >
          <X className="size-4" aria-hidden="true" />
        </button>
      </div>
      {error && (
        <p role="alert" className="text-xs text-critical">
          {error}
        </p>
      )}
    </form>
  );
}