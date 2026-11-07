import type { Metadata } from "next";
import { PageHeader } from "@/components/ui/page-header";
import { AttentionQueue } from "@/components/attention/attention-queue";

export const metadata: Metadata = { title: "Attention Queue · LedgerSense" };

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