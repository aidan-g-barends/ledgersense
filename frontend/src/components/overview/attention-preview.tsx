"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ArrowRight, ShieldCheck } from "lucide-react";
import { listFlags } from "@/lib/flags";
import type { AnomalyFlag } from "@/lib/types";
import { formatMoney } from "@/lib/money";
import { card } from "@/lib/ui";
import { SEVERITY_RANK, SEVERITY_STYLES } from "@/components/attention/flag-styles";

// NEW: the 3 most urgent open flags, with a link to the full queue
export function AttentionPreview() {
  const [flags, setFlags] = useState<AnomalyFlag[] | null>(null); // null = loading
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let ignore = false;
    listFlags("OPEN")
      .then((result) => {
        if (!ignore) setFlags(result);
      })
      .catch(() => {
        if (!ignore) setFailed(true);
      });
    return () => {
      ignore = true;
    };
  }, []);

  const top = useMemo(
    () => [...(flags ?? [])].sort((a, b) => SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity]).slice(0, 3),
    [flags],
  );

  return (
    <section className={`${card} p-4`}>
      <div className="flex items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-fg">
          Needs a second look
          {flags && flags.length > 0 && <span className="ml-2 font-mono text-xs text-fg-muted">{flags.length}</span>}
        </h2>
        <Link href="/attention" className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:text-primary-hover">
          View all <ArrowRight className="size-4" aria-hidden="true" />
        </Link>
      </div>

      {flags === null && !failed && (
        <div className="mt-3 space-y-2" aria-busy="true">
          {[0, 1, 2].map((i) => <div key={i} className="h-10 animate-pulse rounded bg-sunken" />)}
        </div>
      )}
      {failed && <p className="mt-3 text-sm text-critical">Could not load flags.</p>}
      {flags?.length === 0 && (
        <p className="mt-3 inline-flex items-center gap-2 text-sm text-positive">
          <ShieldCheck className="size-4" aria-hidden="true" /> All clear. Nothing needs review.
        </p>
      )}

      {top.length > 0 && (
        <ul className="mt-2 divide-y divide-line">
          {top.map((flag) => {
            const severity = SEVERITY_STYLES[flag.severity];
            return (
              <li key={flag.id} className="flex items-center gap-3 py-2.5">
                <span className={`shrink-0 rounded border px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] ${severity.chip}`}>
                  {severity.label}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm text-fg">{flag.title}</p>
                  <p className="truncate text-xs text-fg-muted">{flag.transaction.description}</p>
                </div>
                <span className="shrink-0 font-mono text-sm tabular-nums text-fg">{formatMoney(flag.transaction.amount)}</span>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}