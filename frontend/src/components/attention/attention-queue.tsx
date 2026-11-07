"use client";

import { useEffect, useMemo, useState } from "react";
import { ShieldCheck } from "lucide-react";
import { listFlags, reviewFlag, USE_MOCK } from "@/lib/flags";
import type { AnomalyFlag, FlagStatus, ReviewDecision } from "@/lib/types";
import { formatMoney } from "@/lib/money";
import { card } from "@/lib/ui";
import { FormError } from "@/components/auth/form-error";
import { EmptyState } from "@/components/ui/empty-state";
import { Segmented } from "@/components/ui/segmented";
import { useToast } from "@/components/ui/toast-provider";
import { FlagCard } from "./flag-card";
import { FlagDrawer } from "./flag-drawer";
import { AttentionToolbar, type RuleFilter } from "./attention-toolbar";
import { SEVERITY_RANK } from "./flag-styles";

const TABS: { value: FlagStatus; label: string }[] = [
  { value: "OPEN", label: "Needs review" },
  { value: "LEGITIMATE", label: "Fine" },
  { value: "DISPUTED", label: "Disputed" },
];

type Result = { key: FlagStatus; flags?: AnomalyFlag[]; error?: string };

export function AttentionQueue() {
  const toast = useToast(); // CHANGED: toasts replace the old `notice` state
  const [status, setStatus] = useState<FlagStatus>("OPEN");
  const [rule, setRule] = useState<RuleFilter>("ALL"); // NEW
  const [result, setResult] = useState<Result | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<AnomalyFlag | null>(null);

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

  const loading = result?.key !== status;

  // CHANGED: filter by rule first. filter() makes a NEW array, so sorting it is safe.
  const flags = useMemo(
    () =>
      (result?.flags ?? [])
        .filter((f) => rule === "ALL" || f.rule === rule)
        .sort((a, b) => SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity] || b.createdAt.localeCompare(a.createdAt)),
    [result, rule],
  );

  const atRisk = useMemo(() => {
    const seen = new Map<string, number>();
    for (const f of flags) seen.set(f.transaction.id, Math.abs(f.transaction.amount));
    return [...seen.values()].reduce((sum, amount) => sum + amount, 0);
  }, [flags]);

  async function handleReview(flag: AnomalyFlag, decision: ReviewDecision) {
    setBusyId(flag.id);
    setError(null);
    try {
      await reviewFlag(flag.id, decision);
      setResult((r) => (r?.flags ? { ...r, flags: r.flags.filter((f) => f.id !== flag.id) } : r));
      setSelected(null);
      toast.success(decision === "LEGITIMATE" ? "Marked as fine." : "Marked as disputed."); // CHANGED
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

      {/* CHANGED: tabs on the left, filter + export on the right */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Segmented options={TABS} value={status} onChange={setStatus} label="Flag status" />
        <AttentionToolbar rule={rule} onRuleChange={setRule} flags={flags} status={status} />
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
          // NEW: a different message when it's the FILTER hiding everything
          title={rule !== "ALL" ? "No flags for this rule" : status === "OPEN" ? "All clear" : "Nothing here yet"}
          description={
            rule !== "ALL"
              ? "Try another rule, or switch back to All rules."
              : status === "OPEN"
                ? "Nothing needs your attention right now. New imports are checked automatically."
                : "Flags you review will show up here."
          }
        />
      )}

      {!loading &&
        flags.map((flag) => (
          <FlagCard key={flag.id} flag={flag} busy={busyId === flag.id} onReview={handleReview} onOpen={setSelected} />
        ))}

      <FlagDrawer
        flag={selected}
        busy={busyId !== null && busyId === selected?.id}
        onClose={() => setSelected(null)}
        onReview={handleReview}
      />
    </div>
  );
}