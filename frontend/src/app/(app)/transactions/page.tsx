import { TransactionsView } from "@/components/transactions/transactions-view";

export default function TransactionsPage() {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight text-fg">Transactions</h1>
        <p className="mt-1 text-sm text-fg-muted">Every imported transaction, newest first.</p>
      </header>
      <TransactionsView />
    </div>
  );
}