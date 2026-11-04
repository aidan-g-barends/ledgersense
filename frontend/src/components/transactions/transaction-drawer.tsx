"use client";

import { useState } from "react";
import { Check, Copy } from "lucide-react";
import { BANK_LABELS, type BankAccount, type Category, type Transaction } from "@/lib/types";
import { formatDate, formatMoney } from "@/lib/money";
import { labelCaps } from "@/lib/ui";
import { Drawer } from "@/components/ui/drawer";

function Detail({ label, value, mono, copyable }: { label: string; value: string; mono?: boolean; copyable?: boolean }) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      // Clipboard blocked (e.g. not https in some browsers). The text is still selectable.
    }
  }

  return (
    <div className="flex items-start justify-between gap-3 px-3 py-2.5">
      <dt className="shrink-0 text-xs text-fg-muted">{label}</dt>
      <dd className={`flex min-w-0 items-center gap-1.5 text-right text-sm text-fg ${mono ? "font-mono text-xs" : ""}`}>
        <span className="break-all">{value}</span>
        {copyable && (
          <button type="button" onClick={copy} aria-label={`Copy ${label.toLowerCase()}`} className="rounded p-1 text-fg-faint hover:bg-raised hover:text-fg">
            {copied ? <Check className="size-3.5 text-positive" aria-hidden="true" /> : <Copy className="size-3.5" aria-hidden="true" />}
          </button>
        )}
      </dd>
    </div>
  );
}

type Props = {
  tx: Transaction | null; // null = closed
  onClose: () => void;
  accountsById: Record<string, BankAccount>;
  categoriesById: Record<string, Category>;
};

// NEW: everything about one transaction. Friday this grows into the anomaly investigation view.
export function TransactionDrawer({ tx, onClose, accountsById, categoriesById }: Props) {
  const account = tx ? accountsById[tx.bankAccountId] : undefined;
  const category = tx?.categoryId ? categoriesById[tx.categoryId] : undefined;

  return (
    <Drawer open={tx !== null} onClose={onClose} title="Transaction">
      {tx && (
        <div className="space-y-6">
          <div>
            <p className={labelCaps}>{tx.amount > 0 ? "Money in" : "Money out"}</p>
            <p className={`mt-1 font-mono text-3xl font-semibold tabular-nums ${tx.amount > 0 ? "text-positive" : "text-fg"}`}>
              {formatMoney(tx.amount)}
            </p>
            <p className="mt-2 text-sm text-fg">{tx.description}</p>
          </div>

          <dl className="divide-y divide-line-subtle rounded border border-line">
            <Detail label="Date" value={formatDate(tx.postedOn)} />
            <Detail
              label="Account"
              value={account ? `${account.name} · ${BANK_LABELS[account.bank]} ••${account.accountLast4}` : "Unknown account"}
            />
            <Detail label="Category" value={category?.name ?? "Uncategorised"} />
            <Detail label="Balance after" value={tx.balance === null ? "Not in statement" : formatMoney(tx.balance)} mono />
            <Detail label="Reference" value={tx.id} mono copyable />
          </dl>

          <p className="text-xs text-fg-faint">
            Flags and merchant history for this transaction will appear here once anomaly detection is live.
          </p>
        </div>
      )}
    </Drawer>
  );
}