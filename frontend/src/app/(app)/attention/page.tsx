import { PageHeader } from "@/components/ui/page-header";
import { AttentionQueue } from "@/components/attention/attention-queue";

export default function AttentionPage() {
  return (
    <>
      <PageHeader
        title="Attention Queue"
        description="Transactions that look unusual. Tell us if each one is fine or needs disputing."
      />
      <AttentionQueue />
    </>
  );
}