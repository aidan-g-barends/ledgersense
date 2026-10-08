import { Settings } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";

export default function SettingsPage() {
  return (
    <>
      <PageHeader title="Settings" description="Business details, bank accounts and detection preferences." />
      <EmptyState
        icon={Settings}
        title="Nothing to configure yet"
        description="Your business profile and bank accounts will be managed here."
      />
    </>
  );
}