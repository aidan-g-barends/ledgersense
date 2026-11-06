"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { getTimeline, USE_MOCK } from "@/lib/insights";
import type { TimelinePoint } from "@/lib/types";
import { formatDate, formatMoney } from "@/lib/money";
import { card } from "@/lib/ui";
import { FormError } from "@/components/auth/form-error";
import { Segmented } from "@/components/ui/segmented";
import { StatTile } from "@/components/overview/stat-tile";
import { NetFlowChart } from "./net-flow-chart";

type Days = 30 | 90 | 180;
const RANGES: { value: Days; label: string }[] = [
  { value: 30, label: "30 days" },
  { value: 90, label: "90 days" },
  { value: 180, label: "180 days" },
];

type Result = { key: Days; points?: TimelinePoint[]; error?: string };

// NEW: the Timeline page. Same fetch pattern as the attention queue: key + ignore flag.
export function TimelineView() {
  const [days, setDays] = useState<Days>(90);
  const [result, setResult] = useState<Result | null>(null);

  useEffect(() => {
    let ignore = false;
    getTimeline(days)
      .then((points) => {
        if (!ignore) setResult({ key: days, points });
      })
      .catch(() => {
        if (!ignore) setResult({ key: days, error: "Could not load the timeline." });
      });
    return () => {
      ignore = true;
    };
  }, [days]);

  const loading = result?.key !== days;
  const points = useMemo(() => result?.points ?? [], [result]);

  // One loop for all four totals
  const totals = useMemo(() => {
    let moneyIn = 0;
    let moneyOut = 0;
    let flaggedDays = 0;
    for (const p of points) {
      moneyIn += p.moneyIn;
      moneyOut += p.moneyOut;
      if (p.flagCount > 0) flaggedDays++;
    }
    return { moneyIn, moneyOut, net: moneyIn - moneyOut, flaggedDays };
  }, [points]);

  const busiest = useMemo(
    () => [...points].filter((p) => p.moneyOut > 0).sort((a, b) => b.moneyOut - a.moneyOut).slice(0, 5),
    [points],
  );

  return (
    <div className="space-y-4">
      {USE_MOCK && (
        <p className="rounded border border-primary/30 bg-primary/10 px-3 py-2 text-xs text-primary">
          Showing sample figures. The live timeline switches on once the backend is built.
        </p>
      )}

      <Segmented options={RANGES} value={days} onChange={setDays} label="Date range" />

      <FormError message={result?.error ?? null} />

      {loading ? (
        <div className="space-y-4" aria-busy="true">
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {[0, 1, 2, 3].map((i) => <div key={i} className="h-24 animate-pulse rounded-lg bg-sunken" />)}
          </div>
          <div className="h-72 animate-pulse rounded-lg bg-sunken" />
        </div>
      ) : (
        !result?.error && (
          <>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <StatTile label="Money in" value={formatMoney(totals.moneyIn)} valueClass="text-positive" />
              <StatTile label="Money out" value={formatMoney(totals.moneyOut)} />
              <StatTile label="Net" value={formatMoney(totals.net)} valueClass={totals.net >= 0 ? "text-positive" : "text-critical"} />
              <StatTile
                label="Flagged days"
                value={String(totals.flaggedDays)}
                valueClass={totals.flaggedDays > 0 ? "text-critical" : "text-fg"}
                hint={totals.flaggedDays > 0 ? <Link href="/attention" className="text-primary hover:text-primary-hover">Review them</Link> : undefined}
              />
            </div>

            <section className={`${card} p-4`}>
              <h2 className="mb-3 text-sm font-semibold text-fg">Net cash flow</h2>
              <NetFlowChart points={points} />
            </section>

            <section className={`${card} p-4`}>
              <h2 className="text-sm font-semibold text-fg">Biggest spending days</h2>
              <ol className="mt-2 divide-y divide-line">
                {busiest.map((p) => (
                  <li key={p.date} className="flex items-center justify-between gap-3 py-2 text-sm">
                    <span className="font-mono text-xs text-fg-muted">{formatDate(p.date)}</span>
                    <span className="font-mono tabular-nums text-fg">{formatMoney(p.moneyOut)}</span>
                  </li>
                ))}
              </ol>
            </section>
          </>
        )
      )}
    </div>
  );
}