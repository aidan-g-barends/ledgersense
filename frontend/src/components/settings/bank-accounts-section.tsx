"use client";

import { useEffect, useState } from "react";
import { Landmark } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { BANK_LABELS, type Bank, type BankAccount } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, btnSecondary, card, inputClass } from "@/lib/ui";

const BANKS = Object.entries(BANK_LABELS) as [Bank, string][];

// NEW: list accounts + add one (GET/POST /api/bank-accounts)
export function BankAccountsSection() {
  const [accounts, setAccounts] = useState<BankAccount[] | null>(null);
  const [adding, setAdding] = useState(false);
  const [name, setName] = useState("");
  const [bank, setBank] = useState<Bank>("ABSA");
  const [last4, setLast4] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    api<BankAccount[]>("/bank-accounts")
      .then((list) => {
        if (!ignore) setAccounts(list);
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : "Could not load bank accounts.");
      });
    return () => {
      ignore = true;
    };
  }, []);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const created = await api<BankAccount>("/bank-accounts", {
        method: "POST",
        body: { name, bank, accountLast4: last4 },
      });
      // WHY no refetch: we already have the new row, so just add it to what's on screen
      setAccounts((list) => [...(list ?? []), created].sort((a, b) => a.name.localeCompare(b.name)));
      setAdding(false);
      setName("");
      setLast4("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not add the account.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section aria-labelledby="accounts-heading" className={`${card} space-y-4 p-5`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 id="accounts-heading" className="text-base font-semibold text-fg">Bank accounts</h2>
          <p className="mt-1 text-sm text-fg-muted">We only store the last 4 digits of each account number.</p>
        </div>
        {!adding && (
          <button type="button" onClick={() => setAdding(true)} className={btnSecondary}>
            Add account
          </button>
        )}
      </div>

      {accounts && accounts.length === 0 && !adding && (
        <p className="text-sm text-fg-muted">No bank accounts yet. Add one to start importing statements.</p>
      )}

      {accounts && accounts.length > 0 && (
        <ul className="divide-y divide-line-subtle rounded border border-line">
          {accounts.map((a) => (
            <li key={a.id} className="flex flex-wrap items-center gap-3 px-3 py-2.5">
              <Landmark className="size-4 shrink-0 text-fg-faint" aria-hidden="true" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-fg">{a.name}</p>
                <p className="font-mono text-xs text-fg-muted">
                  {BANK_LABELS[a.bank]} ••{a.accountLast4}
                </p>
              </div>
              {/* WHY a badge: tells the user at a glance whether imports need column mapping */}
              {a.hasCsvPreset ? (
                <span className="rounded border border-positive/30 bg-positive/10 px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-positive">
                  CSV preset
                </span>
              ) : (
                <span className="rounded border border-line px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-fg-muted">
                  Manual mapping
                </span>
              )}
            </li>
          ))}
        </ul>
      )}

      {adding && (
        <form onSubmit={handleAdd} className="space-y-3 rounded border border-line bg-sunken p-4">
          <div className="grid gap-3 sm:grid-cols-3">
            <input
              aria-label="Account name"
              placeholder="e.g. Business Cheque"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className={inputClass}
            />
            <select aria-label="Bank" value={bank} onChange={(e) => setBank(e.target.value as Bank)} className={inputClass}>
              {BANKS.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
            <input
              aria-label="Last 4 digits of account number"
              placeholder="Last 4 digits"
              required
              inputMode="numeric"
              maxLength={4}
              pattern="[0-9]{4}"
              value={last4}
              onChange={(e) => setLast4(e.target.value.replace(/\D/g, ""))}
              className={`${inputClass} font-mono`}
            />
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={busy} className={btnPrimary}>
              Save account
            </button>
            <button type="button" onClick={() => setAdding(false)} className={btnSecondary}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <FormError message={error} />
    </section>
  );
}