import type { MonthTotal } from "@/lib/types";
import { formatMoney, formatMonth } from "@/lib/money";
import { card } from "@/lib/ui";

// NEW: money in vs money out per month, as paired bars
export function CashFlowChart({ months }: { months: MonthTotal[] }) {
  // WHY the 1: avoids dividing by 0 if every month is empty
  const max = Math.max(1, ...months.flatMap((m) => [m.moneyIn, m.moneyOut]));
  const pct = (value: number) => `${(value / max) * 100}%`;

  return (
    <section className={`${card} p-4`}>
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-fg">Cash flow</h2>
        <div className="flex gap-4 text-xs text-fg-muted" aria-hidden="true">
          <span className="inline-flex items-center gap-1.5"><span className="size-2 rounded-sm bg-positive" />Money in</span>
          <span className="inline-flex items-center gap-1.5"><span className="size-2 rounded-sm bg-primary" />Money out</span>
        </div>
      </div>

      {/* The picture. aria-hidden because the table below says the same thing in words. */}
      <div className="mt-4 flex items-end gap-2" aria-hidden="true">
        {months.map((m) => (
          <div key={m.month} className="flex flex-1 flex-col items-center gap-2">
            {/* WHY a fixed h-36: percentage heights need a parent with a known height */}
            <div className="flex h-36 w-full items-end justify-center gap-1">
              <div className="w-1/3 max-w-4 rounded-t bg-positive" style={{ height: pct(m.moneyIn) }} title={`In ${formatMoney(m.moneyIn)}`} />
              <div className="w-1/3 max-w-4 rounded-t bg-primary" style={{ height: pct(m.moneyOut) }} title={`Out ${formatMoney(m.moneyOut)}`} />
            </div>
            <span className="font-mono text-[10px] text-fg-faint">{formatMonth(m.month)}</span>
          </div>
        ))}
      </div>

      <table className="sr-only">
        <caption>Money in and out per month</caption>
        <thead>
          <tr><th>Month</th><th>Money in</th><th>Money out</th></tr>
        </thead>
        <tbody>
          {months.map((m) => (
            <tr key={m.month}>
              <td>{formatMonth(m.month, "long")}</td>
              <td>{formatMoney(m.moneyIn)}</td>
              <td>{formatMoney(m.moneyOut)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}