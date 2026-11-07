"use client";

import { Check, LoaderCircle, PanelRightOpen, ShieldAlert } from "lucide-react";
import type { AnomalyFlag, ReviewDecision } from "@/lib/types";
import { formatDate, formatDateTime, formatMoney, formatRelative } from "@/lib/money";
import { btnSecondary, card, labelCaps } from "@/lib/ui";
import { RULE_LABELS, SEVERITY_STYLES } from "./flag-styles";

type Props = {
  flag: AnomalyFlag;
  busy: boolean;
  onReview: (flag: AnomalyFlag, decision: ReviewDecision) => void;
  onOpen: (flag: AnomalyFlag) => void; // opens the details drawer
};

// NEW: one flag. The left stripe colour = severity (the design system's "anomaly callout").
export function FlagCard({ flag, busy, onReview, onOpen }: Props) {
  const severity = SEVERITY_STYLES[flag.severity];
  const tx = flag.transaction;

  return (
    <article className={`${card} border-l-[3px] ${severity.stripe} space-y-4 p-4`}>
      <header className="flex flex-wrap items-center gap-2">
        <span className={`rounded border px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] ${severity.chip}`}>
          {severity.label}
        </span>
        <span className={labelCaps}>{RULE_LABELS[flag.rule]}</span>
        {/* Also shows when it was flagged. Hover for the exact time. */}
        <span className="ml-auto font-mono text-xs text-fg-faint" title={`Flagged ${formatDateTime(flag.createdAt)}`}>
          {formatDate(tx.postedOn)} · flagged {formatRelative(flag.createdAt)}
        </span>
      </header>

      <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1">
        <div className="min-w-0">
          <h2 className="text-base font-semibold text-fg">{flag.title}</h2>
          <p className="mt-0.5 truncate text-sm text-fg-muted">{tx.description}</p>
        </div>
        <p className="font-mono text-lg font-semibold tabular-nums text-fg">{formatMoney(tx.amount)}</p>
      </div>

      <p className="text-sm text-fg">{flag.explanation}</p>

      {/* The numbers behind the flag. Mono = easy to compare at a glance. */}
      <dl className="grid gap-x-4 gap-y-2 rounded border border-line bg-sunken p-3 sm:grid-cols-3">
        {flag.evidence.map((item) => (
          <div key={item.label} className="min-w-0">
            <dt className={labelCaps}>{item.label}</dt>
            <dd className="mt-0.5 font-mono text-sm text-fg">{item.value}</dd>
          </div>
        ))}
      </dl>

      {flag.status === "OPEN" ? (
        <div className="flex flex-wrap gap-2">
          <button type="button" disabled={busy} onClick={() => onReview(flag, "LEGITIMATE")} className={btnSecondary}>
            {busy ? <LoaderCircle className="size-4 animate-spin" aria-hidden="true" /> : <Check className="size-4" aria-hidden="true" />}
            It&apos;s fine
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={() => onReview(flag, "DISPUTED")}
            className="inline-flex h-9 items-center justify-center gap-2 rounded border border-critical/50 bg-surface px-4 text-sm font-medium text-critical transition-colors hover:bg-critical/10 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <ShieldAlert className="size-4" aria-hidden="true" />
            Dispute
          </button>
          <button type="button" onClick={() => onOpen(flag)} className={`${btnSecondary} ml-auto`}>
            <PanelRightOpen className="size-4" aria-hidden="true" />
            Details
          </button>
        </div>
      ) : (
        <div className="flex flex-wrap items-center justify-between gap-2">
          <p className="text-xs text-fg-faint">
            Marked {flag.status === "LEGITIMATE" ? "as fine" : "as disputed"}
            {flag.reviewedAt && ` on ${formatDateTime(flag.reviewedAt)}`}
          </p>
          <button type="button" onClick={() => onOpen(flag)} className={btnSecondary}>
            <PanelRightOpen className="size-4" aria-hidden="true" />
            Details
          </button>
        </div>
      )}
    </article>
  );
}