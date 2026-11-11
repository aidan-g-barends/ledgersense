"use client";

import Link from "next/link";
import { CircleCheck, TriangleAlert } from "lucide-react";
import type { ImportResult } from "@/lib/types";
import { btnPrimary, btnSecondary, card, labelCaps } from "@/lib/ui";

// Step 3. What actually happened.
export function ResultPanel({ result, onReset }: { result: ImportResult; onReset: () => void }) {
  const stats = [
    { label: "Imported", value: result.imported, tone: "text-positive" },
    { label: "Already imported", value: result.duplicates, tone: "text-fg" },
    { label: "Skipped (problems)", value: result.errorCount, tone: result.errorCount > 0 ? "text-warning" : "text-fg" },
    // NEW: red when something needs a look, so it's the first number your eye lands on
    { label: "Flagged", value: result.flagged, tone: result.flagged > 0 ? "text-critical" : "text-fg" },
  ];

  return (
    <section className={`${card} space-y-5 p-6 text-center`}>
      <CircleCheck className="mx-auto size-10 text-positive" aria-hidden="true" />
      <div>
        <h2 className="text-lg font-semibold text-fg">Import complete</h2>
        <p className="mt-1 font-mono text-xs text-fg-muted">{result.fileName}</p>
      </div>

      {/* CHANGED: 4 stats now. 2 columns on phones, 4 on wider screens. */}
      <dl className="mx-auto grid max-w-xl grid-cols-2 gap-3 sm:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label} className="rounded border border-line bg-sunken p-3">
            <dt className={labelCaps}>{s.label}</dt>
            <dd className={`mt-1 font-mono text-xl font-semibold tabular-nums ${s.tone}`}>{s.value}</dd>
          </div>
        ))}
      </dl>

      {/* NEW: if anything was flagged, the main button goes to the Attention Queue instead */}
      <div className="flex flex-wrap justify-center gap-2">
        {result.flagged > 0 ? (
          <>
            <Link href="/attention" className={btnPrimary}>
              <TriangleAlert className="size-4" aria-hidden="true" />
              Review {result.flagged} flag{result.flagged > 1 ? "s" : ""}
            </Link>
            <Link href="/transactions" className={btnSecondary}>
              View transactions
            </Link>
          </>
        ) : (
          <Link href="/transactions" className={btnPrimary}>
            View transactions
          </Link>
        )}
        <button type="button" onClick={onReset} className={btnSecondary}>
          Import another
        </button>
      </div>
    </section>
  );
}