import type { Metadata } from "next";
import { PageHeader } from "@/components/ui/page-header";
import { BusinessSection } from "@/components/settings/business-section";
import { BankAccountsSection } from "@/components/settings/bank-accounts-section";
import { RulesSection } from "@/components/settings/rules-section";
import { ActivitySection } from "@/components/settings/activity-section"; // NEW

export const metadata: Metadata = { title: "Settings · LedgerSense" };

export default function SettingsPage() {
  return (
    <>
      <PageHeader title="Settings" description="Business details, bank accounts and categorisation rules." />
      <div className="max-w-3xl space-y-6">
        <BusinessSection />
        <BankAccountsSection />
        <RulesSection />
        <ActivitySection />
      </div>
    </>
  );
}