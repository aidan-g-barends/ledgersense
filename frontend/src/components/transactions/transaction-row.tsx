import type { Category, Transaction } from "@/lib/types";
import { formatDate, formatMoney } from "@/lib/money";

export type TransactionRowProps = {
  tx: Transaction;
  categories: Category[];
  categoriesById: Record<string, Category>;
  onChanged: () => void;
};

// NEW: one table row. Props for categories/onChanged are already here for Friday.
export function TransactionRow({ tx, categoriesById }: TransactionRowProps) {
  const category = tx.categoryId ? categoriesById[tx.categoryId] : undefined;

  return (
    <tr className="border-t border-line-subtle transition-colors hover:bg-raised">
      <td className="whitespace-nowrap px-4 py-2.5 font-mono text-xs text-fg-muted">{formatDate(tx.postedOn)}</td>
      <td className="px-4 py-2.5 text-sm text-fg">{tx.description}</td>
      <td className="px-4 py-2.5">
        {category ? (
          <span className="rounded border border-line bg-sunken px-1.5 py-0.5 text-xs text-fg">{category.name}</span>
        ) : (
          <span className="text-xs text-fg-faint">Uncategorised</span>
        )}
      </td>
      <td
        className={`whitespace-nowrap px-4 py-2.5 text-right font-mono text-sm tabular-nums ${
          tx.amount > 0 ? "text-positive" : "text-fg"
        }`}
      >
        {tx.amount > 0 ? "+" : ""}
        {formatMoney(tx.amount)}
      </td>
    </tr>
  );
}