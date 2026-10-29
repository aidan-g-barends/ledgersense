"use client";

import { LoaderCircle } from "lucide-react";
import type { ImportPreview } from "@/lib/types";
import { formatDate, formatMoney } from "@/lib/money";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, btnSecondary, card, labelCaps } from "@/lib/ui";

const TONES = { neutral: "text-fg", positive: "text-positive", critical: "text-critical" };

function Stat({ label, value, tone = "neutral" }: { label: string; value: number; tone?: keyof typeof TONES }) {
  return (
    <div className="rounded border border-line bg-sunken p-3">
      <p className={labelCaps}>{label}</p>
      <p className={`mt-1 font-mono text-2xl font-semibold tabular-nums ${TONES[tone]}`}>{value}</p>
    </div>
  );
}

type Props = {
  preview: ImportPreview;
  busy: boolean;
  error: string | null;
  onBack: () => void;
  onConfirm: () => void;
};

// NEW: step 2 of the wizard. Nothing is saved yet: this is what WOULD be imported.
export function PreviewPanel({ preview, busy, error, onBack, onConfirm }: Props) {
  return (
    <section className={`${card} space-y-5 p-5`}>
      <div className="grid gap-3 sm:grid-cols-3">
        <Stat label="Rows found" value={preview.totalRows} />
        <Stat label="Ready to import" value={preview.validRows} tone="positive" />
        <Stat label="Problems" value={preview.errorCount} tone={preview.errorCount > 0 ? "critical" : "neutral"} />
      </div>

      {preview.errors.length > 0 && (
        <div className="rounded border border-warning/30 bg-warning/10 p-3">
          <p className="text-sm font-medium text-warning">These rows will be skipped</p>
          <ul className="mt-2 max-h-40 space-y-1 overflow-y-auto font-mono text-xs text-fg-muted">
            {preview.errors.map((e, i) => (
              <li key={`${e.rowNumber}-${i}`}>
                Row {e.rowNumber}: {e.message}
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="overflow-x-auto rounded border border-line">
        <table className="w-full text-left">
          <thead className="bg-sunken">
            <tr>
              <th className={`${labelCaps} px-3 py-2`}>Date</th>
              <th className={`${labelCaps} px-3 py-2`}>Description</th>
              <th className={`${labelCaps} px-3 py-2 text-right`}>Amount</th>
            </tr>
          </thead>
          <tbody>
            {preview.rows.map((row) => (
              <tr key={row.rowNumber} className="border-t border-line-subtle">
                <td className="whitespace-nowrap px-3 py-2 font-mono text-xs text-fg-muted">{formatDate(row.postedOn)}</td>
                <td className="px-3 py-2 text-sm text-fg">{row.description}</td>
                <td className={`whitespace-nowrap px-3 py-2 text-right font-mono text-sm tabular-nums ${row.amount > 0 ? "text-positive" : "text-fg"}`}>
                  {formatMoney(row.amount)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {preview.validRows > preview.rows.length && (
        <p className="text-xs text-fg-faint">
          Showing the first {preview.rows.length} of {preview.validRows} rows.
        </p>
      )}
      <p className="text-xs text-fg-muted">Rows you&apos;ve imported before are skipped automatically.</p>

      <FormError message={error} />

      <div className="flex justify-between gap-2">
        <button type="button" onClick={onBack} disabled={busy} className={btnSecondary}>
          Back
        </button>
        <button type="button" onClick={onConfirm} disabled={busy || preview.validRows === 0} className={btnPrimary}>
          {busy && <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />}
          Import {preview.validRows} transactions
        </button>
      </div>
    </section>
  );
}