import Link from "next/link";
import { FileUp } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";

export default function OverviewPage() {
  return (
    <>
      <PageHeader
        title="Overview"
        description="Your spending at a glance, and anything that needs a second look."
      />
      <EmptyState
        icon={FileUp}
        title="No transactions yet"
        description="Import a bank statement CSV to see categorized spending and flagged anomalies here."
        action={
          <Link
            href="/imports"
            className="inline-flex h-9 items-center rounded bg-primary px-4 text-sm font-medium text-white transition-colors hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
          >
            Import your first statement
          </Link>
        }
      />
    </>
  );
}