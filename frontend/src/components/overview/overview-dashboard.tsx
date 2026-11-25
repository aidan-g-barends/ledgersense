"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { FileUp } from "lucide-react";
import { getInsightsSummary, USE_MOCK } from "@/lib/insights";
import type { InsightsSummary } from "@/lib/types";
import { formatMoney, formatMonth } from "@/lib/money";
import { btnPrimary } from "@/lib/ui";
import { EmptyState } from "@/components/ui/empty-state";
import { FormError } from "@/components/auth/form-error";
import { StatTile } from "./stat-tile";
import { CashFlowChart } from "./cash-flow-chart";
import { TopCategories } from "./top-categories";
import { AttentionPreview } from "./attention-preview";

type Result = { summary?: InsightsSummary; error?: string };

// NEW: the Overview page. Fetches once, then hands slices of the summary to each card.
export function OverviewDashboard() {
  const [result, setResult] = useState<Result | null>(null); // null = loading

  useEffect(() => {
    let ignore = false;
    getInsightsSummary()
      .then((summary) => {
        if (!ignore) setResult({ summary });
      })
      .catch(() => {
        if (!ignore) setResult({ error: "Could not load your overview." });
      });
    return () => {
      ignore = true;
    };
  }, []);

  if (result === null) {
    return (
      <div className="space-y-4" aria-busy="true">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {[0, 1, 2, 3].map((i) => <div key={i} className="h-24 animate-pulse rounded-lg bg-sunken" />)}
        </div>
        <div className="h-64 animate-pulse rounded-lg bg-sunken" />
      </div>
    );
  }

  const summary = result.summary;
  if (!summary) return <FormError message={result.error ?? "Something went wrong."} />;

  // No data yet: keep the friendly "import your first CSV" screen from before
  if (summary.transactionCount === 0) {
    return (
      <EmptyState
        icon={FileUp}
        title="No transactions yet"
        description="Import a bank statement CSV to see categorised spending and flagged anomalies here."
        action={<Link href="/imports" className={btnPrimary}>Import a CSV</Link>}
      />
    );
  }

  const previous = summary.months.at(-2); // the month before this one, for the "vs Sep" hint
  const change = summary.moneyOutChangePct;

  return (
    <div className="space-y-4">
      {USE_MOCK && (
        <p className="rounded border border-primary/30 bg-primary/10 px-3 py-2 text-xs text-primary">
          Showing sample figures. Live insights switch on once the backend is built.
        </p>
      )}

      <p className="font-mono text-xs text-fg-muted">{formatMonth(summary.month, "long")}</p>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <StatTile label="Money in" value={formatMoney(summary.moneyIn)} valueClass="text-positive" />
        <StatTile
          label="Money out"
          value={formatMoney(summary.moneyOut)}
          hint={
            change !== null && previous ? (
              // WHY these colours: for spending, going UP is the bad direction
              <span className={change > 0 ? "text-warning" : "text-positive"}>
                {change > 0 ? "+" : ""}
                {change.toFixed(1)}% vs {formatMonth(previous.month)}
              </span>
            ) : undefined
          }
        />
        <StatTile
          label="Net"
          value={formatMoney(summary.net)}
          valueClass={summary.net >= 0 ? "text-positive" : "text-critical"}
        />
        <StatTile label="Transactions" value={String(summary.transactionCount)} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <CashFlowChart months={summary.months} />
        </div>
        <TopCategories categories={summary.topCategories} moneyOut={summary.moneyOut} />
      </div>

      <AttentionPreview />
    </div>
  );
}