import type { ReactNode } from "react";
import { card, labelCaps } from "@/lib/ui";

type Props = {
  label: string;
  value: string;
  valueClass?: string; // e.g. "text-positive" for a good number
  hint?: ReactNode;    // small line under the value
};

// NEW: one number on the Overview. No "use client": it has no state, so it can render on the server.
export function StatTile({ label, value, valueClass = "text-fg", hint }: Props) {
  return (
    <div className={`${card} p-4`}>
      <p className={labelCaps}>{label}</p>
      <p className={`mt-1.5 font-mono text-xl font-semibold tabular-nums ${valueClass}`}>{value}</p>
      {hint && <p className="mt-1 text-xs text-fg-muted">{hint}</p>}
    </div>
  );
}