"use client";

import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { BANK_LABELS, type Bank, type BankAccount } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, btnSecondary, inputClass } from "@/lib/ui";

type Props = {
  accounts: BankAccount[];
  value: string;
  onChange: (id: string) => void;
  onCreated: (account: BankAccount) => void;
};

// NEW: pick an existing account, or add one inline (first-time users have none)
export function AccountPicker({ accounts, value, onChange, onCreated }: Props) {
  const [adding, setAdding] = useState(accounts.length === 0);
  const [name, setName] = useState("");
  const [bank, setBank] = useState<Bank>("ABSA");
  const [last4, setLast4] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const account = await api<BankAccount>("/bank-accounts", {
        method: "POST",
        body: { name, bank, accountLast4: last4 },
      });
      onCreated(account);
      setAdding(false);
      setName("");
      setLast4("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not add the account.");
    } finally {
      setBusy(false);
    }
  }

  if (!adding) {
    return (
      <div className="flex flex-col gap-2 sm:flex-row">
        <select
          aria-label="Bank account"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={inputClass}
        >
          <option value="">Choose an account…</option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {a.name} · {BANK_LABELS[a.bank]} ••{a.accountLast4}
            </option>
          ))}
        </select>
        <button type="button" onClick={() => setAdding(true)} className={btnSecondary}>
          Add account
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleCreate} className="space-y-3 rounded border border-line bg-sunken p-4">
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
          {Object.entries(BANK_LABELS).map(([value, label]) => (
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
          onChange={(e) => setLast4(e.target.value.replace(/\D/g, ""))} // digits only
          className={`${inputClass} font-mono`}
        />
      </div>
      <p className="text-xs text-fg-faint">We only ever store the last 4 digits of your account number.</p>
      <FormError message={error} />
      <div className="flex gap-2">
        <button type="submit" disabled={busy} className={btnPrimary}>
          Save account
        </button>
        {accounts.length > 0 && (
          <button type="button" onClick={() => setAdding(false)} className={btnSecondary}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}