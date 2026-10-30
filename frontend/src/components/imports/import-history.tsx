"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { ImportBatch } from "@/lib/types";
import { formatDateTime } from "@/lib/money";
import { card, labelCaps } from "@/lib/ui";

const STATUS_STYLES: Record<ImportBatch["status"], string> = {
  COMPLETED: "border-positive/30 bg-positive/10 text-positive",
  PENDING: "border-warning/30 bg-warning/10 text-warning",
  FAILED: "border-critical/30 bg-critical/10 text-critical",
};

type Result = { key: number; batches?: ImportBatch[]; error?: string };

// NEW: refreshKey changes after each import, so the list reloads itself
export function ImportHistory({ refreshKey }: { refreshKey: number }) {
  const [result, setResult] = useState<Result | null>(null);

  useEffect(() => {
    let ignore = false;
    api<ImportBatch[]>("/imports")
      .then((batches) => {
        if (!ignore) setResult({ key: refreshKey, batches });
      })
      .catch((err) => {
        if (!ignore)
          setResult({ key: refreshKey, error: err instanceof ApiError ? err.message : "Could not load history." });
      });
    return () => {
      ignore = true;
    };
  }, [refreshKey]);

  if (!result) return null;
  if (result.error) return <p className="text-sm text-critical">{result.error}</p>;
  if (!result.batches || result.batches.length === 0) return null;

  return (
    <section className={`${card} overflow-hidden`}>
      <h2 className="border-b border-line px-4 py-3 text-sm font-semibold text-fg">Recent imports</h2>
      <div className="overflow-x-auto">
        <table className="w-full text-left">
          <thead className="bg-sunken">
            <tr>
              <th className={`${labelCaps} px-4 py-2`}>When</th>
              <th className={`${labelCaps} px-4 py-2`}>File</th>
              <th className={`${labelCaps} px-4 py-2 text-right`}>Imported</th>
              <th className={`${labelCaps} px-4 py-2 text-right`}>Skipped</th>
              <th className={`${labelCaps} px-4 py-2`}>Status</th>
            </tr>
          </thead>
          <tbody>
            {result.batches.map((b) => (
              <tr key={b.id} className="border-t border-line-subtle">
                <td className="whitespace-nowrap px-4 py-2.5 font-mono text-xs text-fg-muted">{formatDateTime(b.createdAt)}</td>
                <td className="px-4 py-2.5 font-mono text-xs text-fg">{b.fileName}</td>
                <td className="px-4 py-2.5 text-right font-mono text-sm tabular-nums text-fg">{b.rowsImported}</td>
                <td className="px-4 py-2.5 text-right font-mono text-sm tabular-nums text-fg-muted">{b.rowsSkipped}</td>
                <td className="px-4 py-2.5">
                  <span className={`rounded border px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] ${STATUS_STYLES[b.status]}`}>
                    {b.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}