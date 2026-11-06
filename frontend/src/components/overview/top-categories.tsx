import type { CategorySpend } from "@/lib/types";
import { formatMoney } from "@/lib/money";
import { card } from "@/lib/ui";

// NEW: where the money went. Bar width = share of this month's total spend.
export function TopCategories({ categories, moneyOut }: { categories: CategorySpend[]; moneyOut: number }) {
  return (
    <section className={`${card} p-4`}>
      <h2 className="text-sm font-semibold text-fg">Top spending</h2>
      {categories.length === 0 ? (
        <p className="mt-3 text-sm text-fg-muted">No spending this month.</p>
      ) : (
        <ul className="mt-4 space-y-3">
          {categories.map((c) => {
            const share = moneyOut > 0 ? (c.amount / moneyOut) * 100 : 0;
            return (
              // WHY c.name in the key: Uncategorised has a null id, but names are unique per org
              <li key={c.categoryId ?? c.name}>
                <div className="flex items-baseline justify-between gap-3 text-sm">
                  <span className={`truncate ${c.categoryId ? "text-fg" : "text-fg-muted italic"}`}>{c.name}</span>
                  <span className="shrink-0 font-mono text-xs tabular-nums text-fg">{formatMoney(c.amount)}</span>
                </div>
                <div className="mt-1.5 flex items-center gap-2">
                  <span className="h-1.5 flex-1 overflow-hidden rounded-full bg-sunken">
                    <span className="block h-full rounded-full bg-primary" style={{ width: `${share}%` }} />
                  </span>
                  <span className="w-10 text-right font-mono text-[10px] text-fg-faint">{share.toFixed(0)}%</span>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}