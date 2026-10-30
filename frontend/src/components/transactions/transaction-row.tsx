"use client";

import { useState } from "react";
import { LoaderCircle } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/money";
import type { TransactionRowProps } from "./transaction-row-types";

type RecategoriseResponse = { merchantTransactionsUpdated: number };

// CHANGED: the category is now a dropdown, with an "apply to merchant" offer after a change
export function TransactionRow({ tx, categories, categoriesById, onChanged }: TransactionRowProps) {
  const [categoryId, setCategoryId] = useState<string | null>(tx.categoryId);
  const [serverCategoryId, setServerCategoryId] = useState<string | null>(tx.categoryId);
  const [busy, setBusy] = useState(false);
  const [offerRemember, setOfferRemember] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  // NEW: React's "adjust state when a prop changes" pattern. When the list reloads with a
  // new category for this row (e.g. another row's "remember" changed it), sync our local copy.
  if (tx.categoryId !== serverCategoryId) {
    setServerCategoryId(tx.categoryId);
    setCategoryId(tx.categoryId);
  }

  function save(newCategoryId: string | null, rememberMerchant: boolean) {
    return api<RecategoriseResponse>(`/transactions/${tx.id}/category`, {
      method: "PATCH",
      body: { categoryId: newCategoryId, rememberMerchant },
    });
  }

  async function handleChange(value: string) {
    const next = value || null; // "" from the "Uncategorised" option = null
    setBusy(true);
    setMessage(null);
    try {
      await save(next, false);
      setCategoryId(next);
      setOfferRemember(tx.merchantId !== null && next !== null);
    } catch (err) {
      setMessage(err instanceof ApiError ? err.message : "Couldn't save that change.");
    } finally {
      setBusy(false);
    }
  }

  async function handleRemember() {
    setBusy(true);
    try {
      const res = await save(categoryId, true);
      setOfferRemember(false);
      setMessage(`Updated ${res.merchantTransactionsUpdated} transactions. Future imports will match too.`);
      onChanged(); // reload the list so other rows show the new category
    } catch (err) {
      setMessage(err instanceof ApiError ? err.message : "Couldn't apply that to the merchant.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <tr className="border-t border-line-subtle align-top transition-colors hover:bg-raised">
      <td className="whitespace-nowrap px-4 py-2.5 font-mono text-xs text-fg-muted">{formatDate(tx.postedOn)}</td>
      <td className="px-4 py-2.5 text-sm text-fg">{tx.description}</td>
      <td className="px-4 py-2.5">
        <div className="flex items-center gap-2">
          <select
            aria-label={`Category for ${tx.description}`}
            value={categoryId ?? ""}
            disabled={busy}
            onChange={(e) => handleChange(e.target.value)}
            className="h-8 max-w-[11rem] rounded border border-line bg-surface px-2 text-xs text-fg outline-none focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-60"
          >
            <option value="">Uncategorised</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {busy && <LoaderCircle className="size-3.5 animate-spin text-fg-faint" aria-hidden="true" />}
        </div>
        {offerRemember && (
          <button
            type="button"
            onClick={handleRemember}
            disabled={busy}
            className="mt-1.5 text-xs font-medium text-primary underline-offset-4 hover:underline"
          >
            Apply to every transaction from this merchant
          </button>
        )}
        {message && (
          <p role="status" className="mt-1.5 text-xs text-fg-muted">
            {message}
          </p>
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