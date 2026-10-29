"use client";

import { useEffect, useState } from "react";
import { FileUp, LoaderCircle } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { BANK_LABELS, type BankAccount, type ImportPreview, type ImportResult } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, card, labelCaps } from "@/lib/ui";
import { AccountPicker } from "./account-picker";
import { appendMapping, EMPTY_MAPPING, MappingForm, mappingProblem, type Mapping } from "./mapping-form";
import { PreviewPanel } from "./preview-panel";
import { ResultPanel } from "./result-panel";

// NEW: a tiny state machine. Only one step is on screen at a time.
type Step = "setup" | "preview" | "done";

function messageOf(err: unknown, fallback: string) {
  return err instanceof ApiError ? err.message : fallback;
}

export function ImportWizard({ onImported }: { onImported?: () => void }) {
  const [accounts, setAccounts] = useState<BankAccount[] | null>(null);
  const [accountId, setAccountId] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [mapping, setMapping] = useState<Mapping>(EMPTY_MAPPING);
  const [step, setStep] = useState<Step>("setup");
  const [preview, setPreview] = useState<ImportPreview | null>(null);
  const [result, setResult] = useState<ImportResult | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load accounts once. WHY "ignore": if the component unmounts before the
  // request finishes, we must not set state on it.
  useEffect(() => {
    let ignore = false;
    api<BankAccount[]>("/bank-accounts")
      .then((list) => {
        if (ignore) return;
        setAccounts(list);
        if (list.length === 1) setAccountId(list[0].id); // only one? pick it for them
      })
      .catch((err) => {
        if (!ignore) setError(messageOf(err, "Could not load your bank accounts."));
      });
    return () => {
      ignore = true;
    };
  }, []);

  const account = accounts?.find((a) => a.id === accountId);
  const needsMapping = account !== undefined && !account.hasCsvPreset;

  function buildForm(): FormData {
    const form = new FormData();
    form.append("file", file as File);
    form.append("bankAccountId", accountId);
    if (needsMapping) appendMapping(form, mapping); // no mapping sent = backend uses the preset
    return form;
  }

  async function handlePreview() {
    setError(null);
    if (!account) {
      setError("Choose a bank account first.");
      return;
    }
    if (!file) {
      setError("Choose a CSV file first.");
      return;
    }
    if (needsMapping) {
      const problem = mappingProblem(mapping);
      if (problem) {
        setError(problem);
        return;
      }
    }
    setBusy(true);
    try {
      setPreview(await api<ImportPreview>("/imports/preview", { method: "POST", body: buildForm() }));
      setStep("preview");
    } catch (err) {
      setError(messageOf(err, "Could not read that file."));
    } finally {
      setBusy(false);
    }
  }

  async function handleImport() {
    setError(null);
    setBusy(true);
    try {
      setResult(await api<ImportResult>("/imports", { method: "POST", body: buildForm() }));
      setStep("done");
      onImported?.(); // ?.() = only call it if the parent passed one
    } catch (err) {
      setError(messageOf(err, "The import failed. Nothing was saved."));
    } finally {
      setBusy(false);
    }
  }

  function reset() {
    setFile(null);
    setPreview(null);
    setResult(null);
    setError(null);
    setStep("setup");
  }

  if (step === "done" && result) {
    return <ResultPanel result={result} onReset={reset} />;
  }

  if (step === "preview" && preview) {
    return (
      <PreviewPanel
        preview={preview}
        busy={busy}
        error={error}
        onBack={() => {
          setError(null);
          setStep("setup");
        }}
        onConfirm={handleImport}
      />
    );
  }

  return (
    <section className={`${card} space-y-6 p-5`}>
      <div className="space-y-2">
        <p className={labelCaps}>1 · Bank account</p>
        {accounts === null ? (
          <p className="flex items-center gap-2 text-sm text-fg-muted">
            <LoaderCircle className="size-4 animate-spin" aria-hidden="true" /> Loading accounts…
          </p>
        ) : (
          <AccountPicker
            accounts={accounts}
            value={accountId}
            onChange={setAccountId}
            onCreated={(created) => {
              setAccounts([...accounts, created]);
              setAccountId(created.id);
            }}
          />
        )}
        {account &&
          (account.hasCsvPreset ? (
            <p className="text-xs text-positive">We know {BANK_LABELS[account.bank]}&apos;s CSV layout, so no setup needed.</p>
          ) : (
            <p className="text-xs text-fg-muted">We don&apos;t know this bank&apos;s layout yet, so tell us the columns below.</p>
          ))}
      </div>

      <div className="space-y-2">
        <p className={labelCaps}>2 · Statement file</p>
        {/* WHY a label around a hidden input: the whole box is clickable, and keyboard users still reach the input */}
        <label className="flex cursor-pointer flex-col items-center gap-2 rounded-lg border border-dashed border-line-strong bg-sunken px-4 py-8 text-center transition-colors hover:border-primary focus-within:border-primary">
          <FileUp className="size-6 text-fg-muted" aria-hidden="true" />
          <span className="text-sm font-medium text-fg">{file ? file.name : "Choose a CSV file"}</span>
          <span className="text-xs text-fg-faint">
            {file ? `${(file.size / 1024).toFixed(1)} KB` : "Export it from your online banking. Max 2MB."}
          </span>
          <input
            type="file"
            accept=".csv,text/csv"
            className="sr-only"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
        </label>
      </div>

      {needsMapping && (
        <div className="space-y-2">
          <p className={labelCaps}>3 · Column layout</p>
          <MappingForm value={mapping} onChange={setMapping} />
        </div>
      )}

      <FormError message={error} />

      <div className="flex justify-end">
        <button type="button" onClick={handlePreview} disabled={busy} className={btnPrimary}>
          {busy && <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />}
          Preview import
        </button>
      </div>
    </section>
  );
}