import type { Metadata } from "next";
import { PageHeader } from "@/components/ui/page-header";
import { OverviewDashboard } from "@/components/overview/overview-dashboard";

export const metadata: Metadata = { title: "Overview · LedgerSense" };

export default function OverviewPage() {
  return (
    <>
      <PageHeader
        title="Overview"
        description="Your spending at a glance, and anything that needs a second look."
      />
      <OverviewDashboard />
    </>
  );
}