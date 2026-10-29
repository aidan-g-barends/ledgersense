"use client";

import Link from "next/link";
import { CircleCheck } from "lucide-react";
import type { ImportResult } from "@/lib/types";
import { btnPrimary, btnSecondary, card, labelCaps } from "@/lib/ui";

// NEW: step 3. What actually happened.
export function ResultPanel({ result, onReset }: { result: ImportResult; onReset: () => void }) {
  const stats = [
    { label: "Imported", value: result.imported, tone: "text-positive" },
    { label: "Already imported", value: result.duplicates, tone: "text-fg" },
    { label: "Skipped (problems)", value: result.errorCount, tone: result.errorCount > 0 ? "text-warning" : "text-fg" },
  ];

  return (
    <section className={`${card} space-y-5 p-6 text-center`}>
      <CircleCheck className="mx-auto size-10 text-positive" aria-hidden="true" />
      <div>
        <h2 className="text-lg font-semibold text-fg">Import complete</h2>
        <p className="mt-1 font-mono text-xs text-fg-muted">{result.fileName}</p>
      </div>

      <dl className="mx-auto grid max-w-md grid-cols-3 gap-3">
        {stats.map((s) => (
          <div key={s.label} className="rounded border border-line bg-sunken p-3">
            <dt className={labelCaps}>{s.label}</dt>
            <dd className={`mt-1 font-mono text-xl font-semibold tabular-nums ${s.tone}`}>{s.value}</dd>
          </div>
        ))}
      </dl>

      <div className="flex justify-center gap-2">
        <Link href="/transactions" className={btnPrimary}>
          View transactions
        </Link>
        <button type="button" onClick={onReset} className={btnSecondary}>
          Import another
        </button>
      </div>
    </section>
  );
}