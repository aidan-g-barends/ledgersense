import { FileUp } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";

export default function ImportsPage() {
  return (
    <>
      <PageHeader title="Imports" description="Upload bank statements and review past imports." />
      <EmptyState
        icon={FileUp}
        title="No imports yet"
        description="Upload a CSV statement exported from your bank's online banking to get started."
      />
    </>
  );
}