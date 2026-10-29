import { ImportWizard } from "@/components/imports/import-wizard";

export default function ImportsPage() {
  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight text-fg">Import a statement</h1>
        <p className="mt-1 text-sm text-fg-muted">
          Upload a CSV from your bank. You&apos;ll see a preview before anything is saved.
        </p>
      </header>
      <ImportWizard />
    </div>
  );
}