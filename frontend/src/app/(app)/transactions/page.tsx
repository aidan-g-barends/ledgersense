import { ReceiptText } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";

export default function TransactionsPage() {
  return (
    <>
      <PageHeader title="Transactions" description="Every imported transaction, categorized." />
      <EmptyState
        icon={ReceiptText}
        title="No transactions yet"
        description="Transactions appear here after you import a bank statement."
      />
    </>
  );
}