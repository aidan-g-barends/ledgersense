"use client";

import { Check, LoaderCircle, ShieldAlert } from "lucide-react";
import type { AnomalyFlag, ReviewDecision } from "@/lib/types";
import { formatDate, formatDateTime, formatMoney } from "@/lib/money";
import { btnSecondary, labelCaps } from "@/lib/ui";
import { Drawer } from "@/components/ui/drawer";
import { RULE_LABELS, SEVERITY_STYLES } from "./flag-styles";

type Props = {
  flag: AnomalyFlag | null; // null = closed
  busy: boolean;
  onClose: () => void;
  onReview: (flag: AnomalyFlag, decision: ReviewDecision) => void;
};

function Row({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-start justify-between gap-3 px-3 py-2.5">
      <dt className="shrink-0 text-xs text-fg-muted">{label}</dt>
      <dd className={`min-w-0 break-all text-right text-sm text-fg ${mono ? "font-mono text-xs" : ""}`}>{value}</dd>
    </div>
  );
}

// The full story behind one flag: why it fired, the evidence, the transaction, and the review actions.
export function FlagDrawer({ flag, busy, onClose, onReview }: Props) {
  return (
    <Drawer open={flag !== null} onClose={onClose} title="Flag details">
      {flag && (
        <div className="space-y-6">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <span
                className={`rounded border px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] ${SEVERITY_STYLES[flag.severity].chip}`}
              >
                {SEVERITY_STYLES[flag.severity].label}
              </span>
              <span className={labelCaps}>{RULE_LABELS[flag.rule]}</span>
            </div>
            <h3 className="text-lg font-semibold text-fg">{flag.title}</h3>
            <p className="text-sm text-fg">{flag.explanation}</p>
          </div>

          <section className="space-y-2">
            <h4 className={labelCaps}>Evidence</h4>
            <dl className="divide-y divide-line-subtle rounded border border-line">
              {flag.evidence.map((item) => (
                <Row key={item.label} label={item.label} value={item.value} mono />
              ))}
            </dl>
          </section>

          <section className="space-y-2">
            <h4 className={labelCaps}>Transaction</h4>
            <dl className="divide-y divide-line-subtle rounded border border-line">
              <Row label="Description" value={flag.transaction.description} />
              <Row label="Date" value={formatDate(flag.transaction.postedOn)} />
              <Row label="Amount" value={formatMoney(flag.transaction.amount)} mono />
              <Row label="Reference" value={flag.transaction.id} mono />
            </dl>
          </section>

          <p className="text-xs text-fg-faint">
            Flagged {formatDateTime(flag.createdAt)}
            {flag.reviewedAt && ` · reviewed ${formatDateTime(flag.reviewedAt)}`}
          </p>

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
            </div>
          ) : (
            <p className="text-sm text-fg-muted">Marked {flag.status === "LEGITIMATE" ? "as fine" : "as disputed"}.</p>
          )}
        </div>
      )}
    </Drawer>
  );
}
