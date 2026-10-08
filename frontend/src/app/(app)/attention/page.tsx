import { ShieldCheck } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";

export default function AttentionPage() {
  return (
    <>
      <PageHeader
        title="Attention Queue"
        description="Flagged transactions waiting for your review."
      />
      <EmptyState
        icon={ShieldCheck}
        title="Nothing needs your attention"
        description="When a transaction looks unusual, such as a duplicate charge or a sudden price increase, it will appear here with an explanation."
      />
    </>
  );
}