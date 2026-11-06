"use client";

import { useEffect, useMemo, useState } from "react";
import { ShieldCheck } from "lucide-react";
import { listFlags, reviewFlag, USE_MOCK } from "@/lib/flags";
import type { AnomalyFlag, FlagStatus, ReviewDecision } from "@/lib/types";
import { formatMoney } from "@/lib/money";
import { FormError } from "@/components/auth/form-error";
import { EmptyState } from "@/components/ui/empty-state";
import { card } from "@/lib/ui";
import { FlagCard } from "./flag-card";
import { SEVERITY_RANK } from "./flag-styles";

const TABS: { status: FlagStatus; label: string }[] = [
  { status: "OPEN", label: "Needs review" },
  { status: "LEGITIMATE", label: "Fine" },
  { status: "DISPUTED", label: "Disputed" },
];

type Result = { key: FlagStatus; flags?: AnomalyFlag[]; error?: string };

export function AttentionQueue() {
  const [status, setStatus] = useState<FlagStatus>("OPEN");
  const [result, setResult] = useState<Result | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    listFlags(status)
      .then((flags) => {
        if (!ignore) setResult({ key: status, flags });
      })
      .catch(() => {
        if (!ignore) setResult({ key: status, error: "Could not load flags." });
      });
    return () => {
      ignore = true;
    };
  }, [status]);

  const loading = result?.key !== status; // same derived-loading trick as the transactions page

  // Critical first, then newest
  const flags = useMemo(
    () =>
      [...(result?.flags ?? [])].sort(
        (a, b) => SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity] || b.createdAt.localeCompare(a.createdAt),
      ),
    [result],
  );

  // NEW: "R x at risk" = total of the flagged transactions. One transaction can have
  // two flags (Seaview Grill), so count each transaction only once.
  const atRisk = useMemo(() => {
    const seen = new Map<string, number>();
    for (const f of flags) seen.set(f.transaction.id, Math.abs(f.transaction.amount));
    return [...seen.values()].reduce((sum, amount) => sum + amount, 0);
  }, [flags]);

  async function handleReview(flag: AnomalyFlag, decision: ReviewDecision) {
    setBusyId(flag.id);
    setError(null);
    setNotice(null);
    try {
      await reviewFlag(flag.id, decision);
      // Remove it from this list straight away. It now lives in the other tab.
      setResult((r) => (r?.flags ? { ...r, flags: r.flags.filter((f) => f.id !== flag.id) } : r));
      setNotice(decision === "LEGITIMATE" ? "Marked as fine." : "Marked as disputed.");
    } catch {
      setError("Could not save your review. Try again.");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="max-w-3xl space-y-4">
      {USE_MOCK && (
        <p className="rounded border border-primary/30 bg-primary/10 px-3 py-2 text-xs text-primary">
          Showing sample flags. Live detection switches on once the backend rules are built.
        </p>
      )}

      {/* Tabs. aria-pressed tells screen readers which one is active. */}
      <div className="inline-flex rounded border border-line bg-sunken p-0.5" role="group" aria-label="Flag status">
        {TABS.map((tab) => (
          <button
            key={tab.status}
            type="button"
            aria-pressed={status === tab.status}
            onClick={() => {
              setStatus(tab.status);
              setNotice(null);
            }}
            className={`rounded px-3 py-1.5 text-sm font-medium transition-colors ${
              status === tab.status ? "bg-surface text-fg shadow-sm" : "text-fg-muted hover:text-fg"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {status === "OPEN" && !loading && flags.length > 0 && (
        <div className={`${card} flex flex-wrap items-baseline gap-x-6 gap-y-1 p-4`}>
          <p className="text-sm text-fg-muted">
            <span className="font-mono text-lg font-semibold tabular-nums text-fg">{flags.length}</span> to review
          </p>
          <p className="text-sm text-fg-muted">
            <span className="font-mono text-lg font-semibold tabular-nums text-critical">{formatMoney(atRisk)}</span> involved
          </p>
        </div>
      )}

      {notice && <p role="status" className="text-sm text-positive">{notice}</p>}
      <FormError message={error ?? result?.error ?? null} />

      {loading && (
        <div className="space-y-3" aria-busy="true">
          {[0, 1, 2].map((i) => (
            <div key={i} className="h-40 animate-pulse rounded-lg bg-sunken" />
          ))}
        </div>
      )}

      {!loading && flags.length === 0 && !result?.error && (
        <EmptyState
          icon={ShieldCheck}
          title={status === "OPEN" ? "All clear" : "Nothing here yet"}
          description={
            status === "OPEN"
              ? "Nothing needs your attention right now. New imports are checked automatically."
              : "Flags you review will show up here."
          }
        />
      )}

      {!loading &&
        flags.map((flag) => (
          <FlagCard key={flag.id} flag={flag} busy={busyId === flag.id} onReview={handleReview} />
        ))}
    </div>
  );
}